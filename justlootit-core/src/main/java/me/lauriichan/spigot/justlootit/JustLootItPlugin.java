package me.lauriichan.spigot.justlootit;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.ArgumentRegistry;
import me.lauriichan.laylib.command.CommandManager;
import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.laylib.json.io.JsonParser;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.laylib.reflection.AccessFailedException;
import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.laylib.reflection.JavaAccess;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.ConditionConstant;
import me.lauriichan.minecraft.pluginbase.IBukkitReflection;
import me.lauriichan.minecraft.pluginbase.command.bridge.BukkitCommandInjectableBridge;
import me.lauriichan.minecraft.pluginbase.command.bridge.BukkitCommandInjectableBridge.CommandDefinition;
import me.lauriichan.minecraft.pluginbase.command.processor.IBukkitCommandProcessor;
import me.lauriichan.minecraft.pluginbase.config.startup.IPropertyIO;
import me.lauriichan.minecraft.pluginbase.config.startup.Property;
import me.lauriichan.minecraft.pluginbase.extension.IConditionMap;
import me.lauriichan.minecraft.pluginbase.resource.source.IDataSource;
import me.lauriichan.minecraft.pluginbase.util.spigot.SpigotUpdater;
import me.lauriichan.minecraft.pluginbase.util.spigot.SpigotUpdater.SpigotUpdaterException;
import me.lauriichan.spigot.justlootit.capability.JustLootItCapabilityProvider;
import me.lauriichan.spigot.justlootit.command.*;
import me.lauriichan.spigot.justlootit.command.argument.*;
import me.lauriichan.spigot.justlootit.command.impl.LootItActor;
import me.lauriichan.spigot.justlootit.command.provider.PluginProvider;
import me.lauriichan.spigot.justlootit.compatibility.data.CompatibilityDataExtension;
import me.lauriichan.spigot.justlootit.compatibility.provider.CompatDependency;
import me.lauriichan.spigot.justlootit.compatibility.provider.Compatibility;
import me.lauriichan.spigot.justlootit.compatibility.provider.ICompatProvider;
import me.lauriichan.spigot.justlootit.convert.ConversionProperties;
import me.lauriichan.spigot.justlootit.convert.JustLootItConverter;
import me.lauriichan.spigot.justlootit.data.CacheLookupTable;
import me.lauriichan.spigot.justlootit.data.CachedInventory;
import me.lauriichan.spigot.justlootit.data.CompatibilityContainer;
import me.lauriichan.spigot.justlootit.data.FrameContainer;
import me.lauriichan.spigot.justlootit.data.StaticContainer;
import me.lauriichan.spigot.justlootit.data.VanillaContainer;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.data.migration.provider.ExtensionStorageMigrator;
import me.lauriichan.spigot.justlootit.input.InputProvider;
import me.lauriichan.spigot.justlootit.input.SimpleChatInputProvider;
import me.lauriichan.spigot.justlootit.listener.ItemFramePacketListener;
import me.lauriichan.spigot.justlootit.message.Messages;
import me.lauriichan.spigot.justlootit.nms.IServiceProvider;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.VersionHelper;
import me.lauriichan.spigot.justlootit.nms.capability.CapabilityManager;
import me.lauriichan.spigot.justlootit.nms.packet.listener.PacketContainer;
import me.lauriichan.spigot.justlootit.nms.packet.listener.PacketManager;
import me.lauriichan.spigot.justlootit.platform.JustLootItPlatform;
import me.lauriichan.spigot.justlootit.platform.folia.FoliaPlatform;
import me.lauriichan.spigot.justlootit.platform.paper.PaperPlatform;
import me.lauriichan.spigot.justlootit.platform.spigot.SpigotPlatform;
import me.lauriichan.spigot.justlootit.storage.StorageAdapterRegistry;
import me.lauriichan.spigot.justlootit.storage.StorageMigrator;
import me.lauriichan.spigot.justlootit.storage.util.cache.CacheTickTimer;
import me.lauriichan.spigot.justlootit.util.JLIInitializationException;
import me.lauriichan.spigot.justlootit.util.PluginVersion;

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

    private final CacheTickTimer levelTickTimer = new CacheTickTimer();
    private final CacheTickTimer playerTickTimer = new CacheTickTimer();
    
    private StorageMigrator storageMigrator;
    private StorageAdapterRegistry levelStorageRegistry;
    private StorageAdapterRegistry playerStorageRegistry;

    private volatile JustLootItPlatform platform;

    private SpigotUpdater<PluginVersion> updater;

    private File mainWorldFolder;

    private VersionHandler versionHandler;
    private VersionHelper versionHelper;
    private PacketManager packetManager;

    private CommandManager commandManager;
    private BukkitCommandInjectableBridge<?> commandBridge;

    private volatile InputProvider inputProvider = SimpleChatInputProvider.CHAT;

    private final Property<Boolean> keepConversionFile = new Property<>("debug.conversion",
        "If set to true, the conversion will never delete its property file.", IPropertyIO.BOOLEAN, false);

    /*
     * PacketContainers
     */

    private PacketContainer itemFrameContainer;

    /*
     * Setup
     */

    @Override
    protected void onPluginProperties(ObjectArrayList<Property<?>> properties) {
        properties.add(keepConversionFile);
    }

    @Override
    protected void onPluginLoad() throws Throwable {
        platform = initPlatform();
        if (!setupVersionHandler()) {
            return;
        }
        setupUpdater();
        DataIO.setup(versionHandler.io());
        setupCapabilities();
        setupTimers();
    }

    private boolean setupVersionHandler() {
        try {
            versionHandler = initVersionHandler();
            versionHelper = versionHandler.versionHelper();
            packetManager = versionHandler.packetManager();
            logger().info("Initialized version support for " + platform.version().coreVersion());
            return true;
        } catch (final JLIInitializationException exp) {
            logger().error("Failed to initialize version support for " + platform.version().coreVersion());
            logger().error("Reason: '" + exp.getMessage() + "'");
            logger().error("");
            logger().error("Can't work like this, disabling...");
            if (exp.getCause() != null) {
                logger().error(exp.getCause());
            }
            actDisabled(true);
            return false;
        }
    }

    private void setupUpdater() {
        IDataSource source = resource("jar://META-INF/updater.json");
        if (!source.exists()) {
            logger().error("Couldn't setup updater, no update information will be available.");
            return;
        }
        JsonObject object;
        try (InputStream input = source.openReadableStream()) {
            object = JsonParser.fromStream(input).asJsonObject();
        } catch (Throwable e) {
            if (logger().isDebug()) {
                logger().error("Couldn't setup updater, no update information will be available.", e);
            } else {
                logger().error("Couldn't setup updater, no update information will be available.");
            }
            return;
        }
        if (!object.has("resource_id")) {
            logger().error("Couldn't setup updater, no update information will be available.");
            logger().debug("No resource id was available");
            return;
        }
        updater = new SpigotUpdater<>(object.getAsInt("resource_id"), getDescription().getVersion(), PluginVersion::new);
    }

    private void setupCapabilities() {
        final CapabilityManager capabilities = versionHandler.capabilities();
        capabilities.add(JustLootItCapabilityProvider.CAPABILITY_PROVIDER);
    }

    private void setupTimers() {
        playerTickTimer.setLength(1, TimeUnit.SECONDS);
        playerTickTimer.setPauseLength(50, TimeUnit.MILLISECONDS);
        playerTickTimer.setName("Cache (Player)");
        levelTickTimer.setLength(1, TimeUnit.SECONDS);
        levelTickTimer.setPauseLength(5, TimeUnit.SECONDS);
        levelTickTimer.setName("Cache (Level)");
    }

    /*
     * Start
     */

    @Override
    protected IBukkitReflection createBukkitReflection() {
        return new JustLootItPlatformReflection(platform);
    }

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
        // Setup storage before conversion
        setupStorage();
        if (doWorldConversion()) {
            // Restart server afterwards, just to be safe
            getServer().spigot().restart();
            return;
        }
        commandManager = new CommandManager(logger(), argumentRegistry());
        commandManager.setPrefix("/jli ");
        commandBridge = new BukkitCommandInjectableBridge<>(IBukkitCommandProcessor.commandLine(), commandManager, this,
            CommandDefinition.of("justlootit").alias("jloot").alias("jli").description("command.description.justlootit.parent").build(this),
            this::actor).inject();
        registerCommands(commandManager);
        // Initialize compatibilities
        initializeCompatibilities();
    }

    private boolean doWorldConversion() {
        ConversionProperties properties = new ConversionProperties(logger(), getConversionPropertyFile(), false);
        if (properties.isAvailable()) {
            boolean conversionWasDone = JustLootItConverter.convert(this, versionHandler, properties);
            if (!keepConversionFile.value()) {
                properties.delete();
            }
            // We only want to return true if the conversion was done, otherwise there is no need to restart
            // Prevent server from starting normally if keepConversionFile is set to true
            return conversionWasDone || keepConversionFile.value();
        }
        return false;
    }

    private void initializeCompatibilities() {
        extension(ICompatProvider.class, true).callInstances(provider -> {
            Compatibility compatibility = provider.getClass().getAnnotation(Compatibility.class);
            if (compatibility == null) {
                return;
            }
            new CompatDependency(compatibility.name(), compatibility.minMajor(), compatibility.maxMajor(), compatibility.minMinor(),
                compatibility.maxMinor(), provider);
        });
        extension(CompatibilityDataExtension.class, true);
    }
    
    private void setupStorage() {
        this.storageMigrator = new ExtensionStorageMigrator(this);
        this.levelStorageRegistry = new StorageAdapterRegistry(storageMigrator);
        this.playerStorageRegistry = new StorageAdapterRegistry(storageMigrator);
        
        // Register level storage adapters
        levelStorageRegistry.register(CompatibilityContainer.ADAPTER);
        levelStorageRegistry.register(VanillaContainer.ADAPTER);
        levelStorageRegistry.register(StaticContainer.ADAPTER);
        levelStorageRegistry.register(FrameContainer.ADAPTER);
        
        // Register player storage adapters
        playerStorageRegistry.register(CachedInventory.ADAPTER);
        playerStorageRegistry.register(CacheLookupTable.ADAPTER);
    }

    @Override
    protected void onPluginReady() {
        mainWorldFolder = Bukkit.getWorlds().get(0).getWorldFolder();
        if (versionHandler != null) {
            versionHandler.ready();
            registerPacketListeners();
        }
        startTimers();
        // Update compatibilities
        CompatDependency.updateAll(this);
        // Check for updates
        runUpdateCheck();
    }

    private void runUpdateCheck() {
        if (updater == null) {
            return;
        }
        platform().scheduler().async(() -> {
            Thread thread = Thread.currentThread();
            String threadName = thread.getName();
            thread.setName("Update checker");
            try {
                Actor<?> console = actor(Bukkit.getConsoleSender());
                if (updater.isUp2Date()) {
                    console.sendTranslatedMessage(Messages.UPDATER_UPDATE_LATEST, Key.of("version.current", updater.getVersion()));
                } else {
                    console.sendTranslatedMessage(Messages.UPDATER_UPDATE_AVAILABLE, Key.of("version.current", updater.getVersion()),
                        Key.of("version.latest", updater.getLatestVersion()));
                }
            } catch (SpigotUpdaterException e) {
                if (logger().isDebug()) {
                    logger().error("Failed to retrieve update information", e);
                } else {
                    logger().error("Failed to retrieve update information");
                }
            } finally {
                thread.setName(threadName);
            }
        });
    }

    private void registerCommands(final CommandManager manager) {
        extension(ICommandExtension.class, false).callClasses(clazz -> manager.register(clazz));
    }

    private void registerPacketListeners() {
        // Register packet listener
        itemFrameContainer = packetManager.register(new ItemFramePacketListener(versionHandler)).setGlobal(true);
    }

    private void startTimers() {
        levelTickTimer.start();
        playerTickTimer.start();
    }

    /*
     * Stop
     */

    @Override
    public void onPluginDisable() {
        stopTimers();
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

    private void stopTimers() {
        levelTickTimer.stop();
        levelTickTimer.clear();
        playerTickTimer.stop();
        playerTickTimer.clear();
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

    public CacheTickTimer levelTickTimer() {
        return levelTickTimer;
    }

    public CacheTickTimer playerTickTimer() {
        return playerTickTimer;
    }
    
    public StorageMigrator storageMigrator() {
        return storageMigrator;
    }

    public StorageAdapterRegistry levelStorageRegistry() {
        return levelStorageRegistry;
    }

    public StorageAdapterRegistry playerStorageRegistry() {
        return playerStorageRegistry;
    }

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

    public File mainWorldFolder() {
        return mainWorldFolder;
    }
    
    public SpigotUpdater<PluginVersion> updater() {
        return updater;
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

    public <T extends CommandSender> LootItActor<T> actor(final T sender) {
        return actor(sender, this);
    }

    private <T extends CommandSender> LootItActor<T> actor(final T sender, final BasePlugin<?> plugin) {
        return new LootItActor<>(sender, (JustLootItPlugin) plugin);
    }

    /*
     * Init Platform
     */

    private JustLootItPlatform initPlatform() {
        if (ClassUtil.findClass("io.papermc.paper.threadedregions.RegionizedServer") != null
            || ClassUtil.findClass("io.papermc.paper.threadedregions.RegionizedServerInitEvent") != null) {
            return new FoliaPlatform(this, logger());
        }
        if (ClassUtil.findClass("com.destroystokyo.paper.PaperConfig") != null
            || ClassUtil.findClass("io.papermc.paper.configuration.Configuration") != null) {
            return new PaperPlatform(this, logger());
        }
        return new SpigotPlatform(this, logger());
    }

    /*
     * Init Version Handler
     */

    private VersionHandler initVersionHandler() throws JLIInitializationException {
        final String path = String.format(VERSION_PATH, platform.version().packageVersion(), platform.version().coreVersion());
        final Class<?> clazz = ClassUtil.findClass(path);
        if (clazz == null || !VersionHandler.class.isAssignableFrom(clazz)) {
            throw new JLIInitializationException("Couldn't find class '" + path + "'!");
        }
        final Constructor<?> constructor = ClassUtil.getConstructor(clazz, IServiceProvider.class);
        if (constructor == null) {
            throw new JLIInitializationException("Couldn't find valid constructor for class '" + path + "'!");
        }
        try {
            return JavaAccess.PLATFORM.invoke(constructor, this);
        } catch (AccessFailedException exception) {
            throw new JLIInitializationException("Failed to initialize VersionHandler!", exception.getCause());
        }
    }

}
