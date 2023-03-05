package me.lauriichan.spigot.justlootit;

import java.lang.reflect.Constructor;
import java.util.concurrent.ExecutorService;

import me.lauriichan.spigot.justlootit.listener.ContainerListener;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import me.lauriichan.laylib.command.ArgumentRegistry;
import me.lauriichan.laylib.command.CommandManager;
import me.lauriichan.laylib.localization.MessageManager;
import me.lauriichan.laylib.localization.source.AnnotationMessageSource;
import me.lauriichan.laylib.localization.source.EnumMessageSource;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.laylib.reflection.JavaAccess;
import me.lauriichan.spigot.justlootit.command.JustLootItCommand;
import me.lauriichan.spigot.justlootit.command.impl.BukkitCommandInjector;
import me.lauriichan.spigot.justlootit.command.provider.LoggerProvider;
import me.lauriichan.spigot.justlootit.command.provider.PluginProvider;
import me.lauriichan.spigot.justlootit.listener.ItemFrameListener;
import me.lauriichan.spigot.justlootit.message.CommandDescription;
import me.lauriichan.spigot.justlootit.message.CommandManagerMessage;
import me.lauriichan.spigot.justlootit.message.Messages;
import me.lauriichan.spigot.justlootit.message.impl.SimpleMessageProviderFactory;
import me.lauriichan.spigot.justlootit.nms.IServiceProvider;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.VersionHelper;
import me.lauriichan.spigot.justlootit.nms.packet.listener.PacketContainer;
import me.lauriichan.spigot.justlootit.nms.packet.listener.PacketManager;
import me.lauriichan.spigot.justlootit.util.BukkitExecutorService;
import me.lauriichan.spigot.justlootit.util.BukkitSimpleLogger;
import me.lauriichan.spigot.justlootit.util.VersionConstant;

public final class JustLootItPlugin extends JavaPlugin implements IServiceProvider {

    /*
     *  TODO List
     * 
     *  - Add support for item containers
     *      - Container with Loottable
     *      - Container without Loottable
     *  - Add support for third-party plugins (add an api or smth)
     *  - Add automatic detection for vanilla containers and item frames
     *  
     *  (Optional)
     *  
     *  - Use CommandManager as main command instead of each command being a main command
     * 
     */
    
    public static JustLootItPlugin get() {
        return getPlugin(JustLootItPlugin.class);
    }

    private static final String VERSION_PATH = JustLootItPlugin.class.getPackageName() + ".nms.%s.VersionHandler%s";



    private final ExecutorService mainService = new BukkitExecutorService(this, false);
    private final ExecutorService asyncService = new BukkitExecutorService(this, true);

    private VersionHandler versionHandler;
    private VersionHelper versionHelper;
    private PacketManager packetManager;
    private String coreVersion;

    private ISimpleLogger logger;
    private CommandManager commandManager;
    private MessageManager messageManager;

    /*
     * PacketContainers
     */

    private PacketContainer itemFrameContainer;

    /*
     * Setup
     */

    @Override
    public void onLoad() {
        setupVersionHandler();
        setupEnvironment();
    }

    public NamespacedKey key(String name) {
        return new NamespacedKey(this, name);
    }

    private void setupEnvironment() {
        logger = new BukkitSimpleLogger(getLogger());
        messageManager = new MessageManager();
        commandManager = new CommandManager(logger);
    }

    private void setupVersionHandler() {
        try {
            versionHandler = initVersionHandler();
            versionHelper = versionHandler.getVersionHelper();
            packetManager = versionHandler.getPacketManager();
            getLogger().severe("Initialized version support for " + coreVersion);
        } catch (Exception exp) {
            getLogger().severe("Failed to initialize version support for " + coreVersion);
            getLogger().severe("Reason: '" + exp.getMessage() + "'");
            getLogger().severe("");
            getLogger().severe("Some features might be disabled because of that");
        }
    }

    /*
     * Start
     */

    @Override
    public void onEnable() {
        commandManager.setInjector(new BukkitCommandInjector(versionHelper, commandManager, messageManager, this));
        registerMessages(messageManager);
        registerArgumentTypes(commandManager.getRegistry());
        registerCommands(commandManager);
        if (versionHandler != null) {
            versionHandler.enable();
        }
        registerListeners(getServer().getPluginManager());
    }

    private void registerMessages(MessageManager manager) {
        final SimpleMessageProviderFactory factory = new SimpleMessageProviderFactory();
        manager.register(new EnumMessageSource(CommandManagerMessage.class, factory));
        manager.register(new EnumMessageSource(CommandDescription.class, factory));
        manager.register(new AnnotationMessageSource(Messages.class, factory));
    }

    private void registerArgumentTypes(ArgumentRegistry registry) {
        // Register argument types

        // Register providers
        registry.setProvider(new PluginProvider(this));
        registry.setProvider(new LoggerProvider(logger));
    }

    private void registerCommands(CommandManager manager) {
        manager.register(JustLootItCommand.class);
    }

    private void registerListeners(PluginManager pluginManager) {
        ItemFrameListener itemFrameListener = new ItemFrameListener();
        ContainerListener containerListener = new ContainerListener();

        pluginManager.registerEvents(itemFrameListener, this);
        pluginManager.registerEvents(containerListener, this);
        if (packetManager != null) {
            itemFrameContainer = packetManager.register(itemFrameListener).setGlobal(true);
        }
    }

    /*
     * Stop
     */

    @Override
    public void onDisable() {
        if (versionHandler != null) {
            packetManager.unregister(itemFrameContainer);
            versionHandler.disable();
        }
    }

    /*
     * Getter
     */

    public VersionHandler versionHandler() {
        return versionHandler;
    }

    public VersionHelper versionHelper() {
        return versionHelper;
    }

    public PacketManager packetManager() {
        return packetManager;
    }

    public String coreVersion() {
        return coreVersion;
    }

    /*
     * ServiceProvider implementation
     */

    @Override
    public Plugin plugin() {
        return this;
    }

    @Override
    public ExecutorService mainService() {
        return mainService;
    }

    @Override
    public ExecutorService asyncService() {
        return asyncService;
    }

    /*
     * Init Version Handler
     */

    private VersionHandler initVersionHandler() {
        String path = String.format(VERSION_PATH, VersionConstant.PACKAGE_VERSION,
            (coreVersion = VersionConstant.PACKAGE_VERSION.substring(1)));
        Class<?> clazz = ClassUtil.findClass(path);
        if (clazz == null || !VersionHandler.class.isAssignableFrom(clazz)) {
            throw new IllegalStateException("Couldn't find class '" + path + "'!");
        }
        Constructor<?> constructor = ClassUtil.getConstructor(clazz, IServiceProvider.class);
        if (constructor == null) {
            throw new IllegalStateException("Couldn't find valid constructor for class '" + path + "'!");
        }
        return (VersionHandler) JavaAccess.instance(constructor, this);
    }

}
