package me.lauriichan.spigot.justlootit;

import java.lang.reflect.Constructor;
import java.util.concurrent.ExecutorService;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import me.lauriichan.laylib.command.ArgumentRegistry;
import me.lauriichan.laylib.command.CommandManager;
import me.lauriichan.laylib.localization.MessageManager;
import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.laylib.reflection.JavaAccess;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.ConditionConstant;
import me.lauriichan.minecraft.pluginbase.command.bridge.BukkitCommandInjectableBridge;
import me.lauriichan.minecraft.pluginbase.command.bridge.BukkitCommandInjectableBridge.CommandDefinition;
import me.lauriichan.minecraft.pluginbase.command.processor.IBukkitCommandProcessor;
import me.lauriichan.minecraft.pluginbase.extension.IConditionMap;
import me.lauriichan.spigot.justlootit.capability.JustLootItCapabilityProvider;
import me.lauriichan.spigot.justlootit.command.DebugCommand;
import me.lauriichan.spigot.justlootit.command.HelpCommand;
import me.lauriichan.spigot.justlootit.command.argument.LootTableArgument;
import me.lauriichan.spigot.justlootit.command.impl.LootItActor;
import me.lauriichan.spigot.justlootit.command.provider.PluginProvider;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.listener.ItemFramePacketListener;
import me.lauriichan.spigot.justlootit.nms.IServiceProvider;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.VersionHelper;
import me.lauriichan.spigot.justlootit.nms.capability.CapabilityManager;
import me.lauriichan.spigot.justlootit.nms.packet.listener.PacketContainer;
import me.lauriichan.spigot.justlootit.nms.packet.listener.PacketManager;
import me.lauriichan.spigot.justlootit.util.BukkitExecutorService;
import me.lauriichan.spigot.justlootit.util.VersionConstant;

public final class JustLootItPlugin extends BasePlugin<JustLootItPlugin> implements IServiceProvider {

    /*
     *  TODO List
     * 
     *  - Add support for third-party plugins (add an api or smth)
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

    private CommandManager commandManager;
    private BukkitCommandInjectableBridge<?> commandBridge;

    private boolean disabled = false;

    /*
     * PacketContainers
     */

    private PacketContainer itemFrameContainer;

    /*
     * Setup
     */

    @Override
    protected void onPluginLoad() throws Throwable {
        if (!setupVersionHandler()) {
            return;
        }
        DataIO.setup(versionHandler.io());
        setupCapabilities();
    }

    private boolean setupVersionHandler() {
        try {
            versionHandler = initVersionHandler();
            versionHelper = versionHandler.versionHelper();
            packetManager = versionHandler.packetManager();
            getLogger().info("Initialized version support for " + coreVersion);
            return true;
        } catch (final Exception exp) {
            getLogger().severe("Failed to initialize version support for " + coreVersion);
            getLogger().severe("Reason: '" + exp.getMessage() + "'");
            getLogger().severe("");
            getLogger().severe("Can't work like this, disabling...");
            this.disabled = true;
            return false;
        }
    }

    private void setupCapabilities() {
        final CapabilityManager capabilities = versionHandler.capabilities();
        capabilities.add(JustLootItCapabilityProvider.CAPABILITY_PROVIDER);
    }

    /*
     * Start
     */
    
    @Override
    protected void onConditionMapSetup(IConditionMap conditionMap) {
        conditionMap.value(ConditionConstant.ENABLE_GUI, true);
    }

    @Override
    protected void onArgumentSetup(ArgumentRegistry registry) {
        // Register argument types
        registry.registerArgumentType(LootTableArgument.class);

        // Register providers
        registry.setProvider(new PluginProvider(this));
    }

    @Override
    public void onPluginEnable() {
        if (disabled) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        logger().setDebug(true);
        JustLootItKey.setup(this);
        commandManager = new CommandManager(logger(), argumentRegistry());
        commandBridge = new BukkitCommandInjectableBridge<>(IBukkitCommandProcessor.commandLine(), commandManager, messageManager(), this,
            CommandDefinition.of("justlootit").alias("jloot").alias("jli").description("command.description.justlootit.parent").build(this), this::actor)
                .inject();
        registerCommands(commandManager);
        if (versionHandler != null) {
            versionHandler.enable();
            registerPacketListeners();
        }
    }

    private void registerCommands(final CommandManager manager) {
        manager.register(HelpCommand.class);
        manager.register(DebugCommand.class);
    }

    private void registerPacketListeners() {
        // Register packet listener
        itemFrameContainer = packetManager.register(new ItemFramePacketListener(versionHandler)).setGlobal(true);
    }

    /*
     * Stop
     */

    @Override
    public void onPluginDisable() {
        if (disabled) {
            return;
        }
        if (versionHandler != null) {
            packetManager.unregister(itemFrameContainer);
            versionHandler.disable();
        }
        if (commandBridge != null) {
            commandBridge.uninject();
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
     * Utility
     */

    public NamespacedKey key(final String name) {
        return new NamespacedKey(this, name);
    }
    
    public <T extends CommandSender> LootItActor<T> actor(final T sender, final MessageManager manager) {
        return new LootItActor<>(sender, manager, versionHelper);
    }

    /*
     * Init Version Handler
     */

    private VersionHandler initVersionHandler() {
        final String path = String.format(VERSION_PATH, VersionConstant.PACKAGE_VERSION,
            coreVersion = VersionConstant.PACKAGE_VERSION.substring(1));
        final Class<?> clazz = ClassUtil.findClass(path);
        if (clazz == null || !VersionHandler.class.isAssignableFrom(clazz)) {
            throw new IllegalStateException("Couldn't find class '" + path + "'!");
        }
        final Constructor<?> constructor = ClassUtil.getConstructor(clazz, IServiceProvider.class);
        if (constructor == null) {
            throw new IllegalStateException("Couldn't find valid constructor for class '" + path + "'!");
        }
        return (VersionHandler) JavaAccess.instance(constructor, this);
    }

}
