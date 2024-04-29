package me.lauriichan.spigot.justlootit;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import me.lauriichan.laylib.command.Actor;
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
import me.lauriichan.spigot.justlootit.command.*;
import me.lauriichan.spigot.justlootit.command.argument.*;
import me.lauriichan.spigot.justlootit.command.impl.LootItActor;
import me.lauriichan.spigot.justlootit.command.provider.PluginProvider;
import me.lauriichan.spigot.justlootit.convert.ConversionProperties;
import me.lauriichan.spigot.justlootit.convert.JustLootItConverter;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.input.InputProvider;
import me.lauriichan.spigot.justlootit.input.SimpleChatInputProvider;
import me.lauriichan.spigot.justlootit.listener.ItemFramePacketListener;
import me.lauriichan.spigot.justlootit.nms.IServiceProvider;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.VersionHelper;
import me.lauriichan.spigot.justlootit.nms.capability.CapabilityManager;
import me.lauriichan.spigot.justlootit.nms.packet.listener.PacketContainer;
import me.lauriichan.spigot.justlootit.nms.packet.listener.PacketManager;
import me.lauriichan.spigot.justlootit.platform.JustLootItPlatform;
import me.lauriichan.spigot.justlootit.platform.folia.FoliaPlatform;
import me.lauriichan.spigot.justlootit.platform.spigot.SpigotPlatform;
import me.lauriichan.spigot.justlootit.util.CompatDependency;
import me.lauriichan.spigot.justlootit.util.VersionConstant;

public final class JustLootItPlugin extends BasePlugin<JustLootItPlugin> implements IServiceProvider {

    /*
     *  TODO: [AFTER RELEASE] Add support for third-party plugins (add an api or smth)
     *  
     *  TODO: [AFTER RELEASE] Possibly add items that create containers
     */

    public static JustLootItPlugin get() {
        return getPlugin(JustLootItPlugin.class);
    }

    private static final String VERSION_PATH = JustLootItPlugin.class.getPackageName() + ".nms.%s.VersionHandler%s";

    private volatile JustLootItPlatform platform;

    private File mainWorldFolder;

    private VersionHandler versionHandler;
    private VersionHelper versionHelper;
    private PacketManager packetManager;
    private String coreVersion;

    private CommandManager commandManager;
    private BukkitCommandInjectableBridge<?> commandBridge;

    private volatile InputProvider inputProvider = SimpleChatInputProvider.CHAT;

    /*
     * PacketContainers
     */

    private PacketContainer itemFrameContainer;

    /*
     * Setup
     */

    @Override
    protected void onPluginLoad() throws Throwable {
        platform = initPlatform();
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
            logger().info("Initialized version support for " + coreVersion);
            return true;
        } catch (final Exception exp) {
            logger().error("Failed to initialize version support for " + coreVersion);
            logger().error("Reason: '" + exp.getMessage() + "'");
            logger().error("");
            logger().error("Can't work like this, disabling...");
            logger().error(exp);
            actDisabled(true);
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
        registry.registerArgumentType(WorldArgument.class);
        registry.registerArgumentType(ConfigArgument.class);
        registry.registerArgumentType(LootTableArgument.class);
        registry.registerArgumentType(CoordinateArgument.class);
        registry.registerArgumentType(RefreshGroupArgument.class);
        registry.registerArgumentType(OfflinePlayerArgument.class);

        // Register providers
        registry.setProvider(new PluginProvider(this));
    }

    @Override
    public void onPluginEnable() {
        JustLootItKey.setup(this);
        if (versionHandler != null) {
            versionHandler.enable();
        }
        if (doWorldConversion()) {
            // Restart server afterwards, just to be safe
            getServer().spigot().restart();
            return;
        }
        commandManager = new CommandManager(logger(), argumentRegistry());
        commandManager.setPrefix("/jli ");
        commandBridge = new BukkitCommandInjectableBridge<>(IBukkitCommandProcessor.commandLine(), commandManager, messageManager(), this,
            CommandDefinition.of("justlootit").alias("jloot").alias("jli").description("command.description.justlootit.parent").build(this),
            this::actor).inject();
        registerCommands(commandManager);
        // Initialize compatibilities
        JustLootItCompatibilities.loadClass();
    }

    private boolean doWorldConversion() {
        ConversionProperties properties = new ConversionProperties(logger(), getConversionPropertyFile(), false);
        if (properties.isAvailable()) {
            boolean conversionWasDone = JustLootItConverter.convert(versionHandler, properties);
            properties.delete();
            // We only want to return true if the conversion was done, otherwise there is no need to restart
            return conversionWasDone;
        }
        return false;
    }

    @Override
    protected void onPluginReady() {
        mainWorldFolder = Bukkit.getWorlds().get(0).getWorldFolder();
        if (versionHandler != null) {
            registerPacketListeners();
        }
        // Update compatibilities
        CompatDependency.updateAll(this);
    }

    private void registerCommands(final CommandManager manager) {
        extension(ICommandExtension.class, false).callClasses(clazz -> manager.register(clazz));
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
        if (versionHandler != null) {
            packetManager.unregister(itemFrameContainer);
            versionHandler.disable();
        }
        if (commandBridge != null) {
            commandBridge.uninject();
        }
        // Ignore any stats here
        configManager().save();
    }

    /*
     * Setter
     */

    public void inputProvider(InputProvider inputProvider) {
        this.inputProvider = Objects.requireNonNull(inputProvider);
    }

    /*
     * Getter
     */

    public JustLootItPlatform platform() {
        return platform;
    }

    public InputProvider inputProvider() {
        return inputProvider;
    }

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

    public File mainWorldFolder() {
        return mainWorldFolder;
    }

    /*
     * ServiceProvider implementation
     */

    @Override
    public Plugin plugin() {
        return this;
    }

    /*
     * Utility
     */

    public File getConversionPropertyFile() {
        return new File(getDataFolder(), "conversion.properties");
    }

    public NamespacedKey key(final String name) {
        return new NamespacedKey(this, name);
    }

    @Override
    public Actor<Player> playerActor(Player player) {
        return actor(player);
    }

    public <T extends CommandSender> LootItActor<T> actor(final T sender) {
        return actor(sender, messageManager());
    }

    public <T extends CommandSender> LootItActor<T> actor(final T sender, final MessageManager manager) {
        return new LootItActor<>(sender, manager, versionHelper);
    }

    /*
     * Init Platform
     */

    private JustLootItPlatform initPlatform() {
        if (ClassUtil.findClass("io.papermc.paper.threadedregions.RegionizedServer") != null
            || ClassUtil.findClass("io.papermc.paper.threadedregions.RegionizedServerInitEvent") != null) {
            return new FoliaPlatform(this, logger());
        }
        return new SpigotPlatform(this, logger());
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
        try {
            return (VersionHandler) JavaAccess.instanceThrows(constructor, this);
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to initialize VersionHandler!", throwable);
        }
    }

}
