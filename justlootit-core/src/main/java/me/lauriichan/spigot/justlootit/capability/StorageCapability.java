package me.lauriichan.spigot.justlootit.capability;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.config.MainConfig;
import me.lauriichan.spigot.justlootit.data.alternation.AlternationAction;
import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.capability.ICapability;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoWorld;
import me.lauriichan.spigot.justlootit.storage.CachedStorage;
import me.lauriichan.spigot.justlootit.storage.IStorage;
import me.lauriichan.spigot.justlootit.storage.StorageAdapterRegistry;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.RAFMultiStorage;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.RAFSingleStorage;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.versionized.RAFSettings;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.versionized.RAFSettings.MigrationSettings;
import me.lauriichan.spigot.justlootit.storage.util.cache.CacheTickTimer;
import me.lauriichan.spigot.justlootit.storage.util.counter.CounterProgress;
import me.lauriichan.spigot.justlootit.util.IOUtil;
import me.lauriichan.spigot.justlootit.util.progress.MultiNotifier;
import me.lauriichan.spigot.justlootit.util.progress.ProgressTracker;

public abstract class StorageCapability implements ICapability {

    private static File migrateWorldFiles(VersionHandler handler, LevelAdapter level, String path) {
        Path newPath = level.dataFolder().toPath().resolve(path);
        if (!Files.exists(newPath)) {
            Path oldPath = new File(level.asBukkit().getWorldFolder(), "data").toPath().resolve(path);
            if (Files.exists(oldPath)) {
                try {
                    handler.logger().error("[MIGRATION] Copying JLI container files of world '{0}' from old data location to new one",
                        level.asBukkit().getName());
                    IOUtil.copy(oldPath, newPath);
                } catch (IOException e) {
                    handler.logger().error("Failed to copy JLI container files of world '{0}' from old data location to new one", e,
                        level.asBukkit().getName());
                }
            } else {
                oldPath = level.asBukkit().getWorldFolder().toPath().resolve(path);
                if (Files.exists(oldPath)) {
                    try {
                        handler.logger().error("[MIGRATION] Copying JLI container files of world '{0}' from old data location to new one",
                            level.asBukkit().getName());
                        IOUtil.copy(oldPath, newPath);
                    } catch (IOException e) {
                        handler.logger().error("Failed to copy JLI container files of world '{0}' from old data location to new one", e,
                            level.asBukkit().getName());
                    }
                }
            }
        }
        return newPath.toFile();
    }

    private static File migrateGlobalFiles(VersionHandler handler, PlayerAdapter player, String path) {
        File mainWorld = handler.mainWorldFolder();
        File globalData = handler.versionHelper().globalDataFolder();
        if (globalData.equals(mainWorld)) {
            return new File(mainWorld, path + '/' + player.getUniqueId().toString() + ".jli");
        }
        Path newPath = globalData.toPath().resolve(path);
        if (!Files.exists(newPath)) {
            Path oldPath = mainWorld.toPath().resolve("data").resolve(path);
            if (Files.exists(oldPath)) {
                try {
                    handler.logger().error("[MIGRATION] Copying JLI player files from old data location to new one");
                    IOUtil.copy(oldPath, newPath);
                } catch (IOException e) {
                    handler.logger().error("Failed to copy JLI player files from old data location to new one", e);
                }
            } else {
                oldPath = mainWorld.toPath().resolve(path);
                if (Files.exists(oldPath)) {
                    try {
                        handler.logger().error("[MIGRATION] Copying JLI player files from old data location to new one");
                        IOUtil.copy(oldPath, newPath);
                    } catch (IOException e) {
                        handler.logger().error("Failed to copy JLI player files from old data location to new one", e);
                    }
                }
            }
        }
        return newPath.resolve(player.getUniqueId().toString() + ".jli").toFile();
    }

    static final class LevelContainerImpl extends StorageCapability {
        public static final MigrationSettings MIGRATION = new MigrationSettings(1024);
        public static final RAFSettings SETTINGS = RAFSettings.builder().migrationSupport(MIGRATION).build();

        public LevelContainerImpl(final VersionHandler handler, final LevelAdapter adapter) {
            super((JustLootItPlugin) handler.plugin(),
                (plugin, registry) -> new RAFMultiStorage(registry, migrateWorldFiles(handler, adapter, "justlootit/containers"), SETTINGS),
                false, true);
        }

        public LevelContainerImpl(final VersionHandler handler, final ProtoWorld world) {
            super((JustLootItPlugin) handler.plugin(), (plugin, registry) -> new RAFMultiStorage(registry,
                new File(world.getWorldFolder(), "data/justlootit/containers"), SETTINGS), false, false);
        }
    }

    static final class PlayerImpl extends StorageCapability {
        public static final MigrationSettings MIGRATION = new MigrationSettings(64);
        public static final RAFSettings SETTINGS = RAFSettings.builder().migrationSupport(MIGRATION).copyBufferBytes(128).valuesPerFile(256)
            .build();

        public PlayerImpl(final VersionHandler handler, final PlayerAdapter adapter) {
            super((JustLootItPlugin) handler.plugin(),
                (plugin, registry) -> new RAFSingleStorage(registry, migrateGlobalFiles(handler, adapter, "justlootit/players"), SETTINGS),
                true, true);
        }
    }

    private final JustLootItPlugin plugin;

    private final CacheTickTimer tickTimer;
    private final boolean player;

    private final MultiNotifier alterationNotifier = new MultiNotifier();
    private final AtomicReference<ProgressTracker> alterationProgress = new AtomicReference<>();

    protected final IStorage storage;

    public StorageCapability(final JustLootItPlugin plugin, final BiFunction<JustLootItPlugin, StorageAdapterRegistry, IStorage> creator,
        final boolean player, final boolean cached) {
        this.plugin = plugin;
        this.tickTimer = player ? plugin.playerTickTimer() : plugin.levelTickTimer();
        this.player = player;
        IStorage storage = creator.apply(plugin, player ? plugin.playerStorageRegistry() : plugin.levelStorageRegistry());
        if (!cached) {
            this.storage = storage;
        } else {
            CachedStorage cachedStorage = new CachedStorage(storage);
            this.storage = cachedStorage;
            updateConfiguration(plugin.configManager().config(MainConfig.class));
            tickTimer.add(cachedStorage.cache());
        }
    }

    public final boolean hasBulkOperationRunning() {
        ProgressTracker tracker = alterationProgress.get();
        if (tracker != null && tracker.progress() != null && !tracker.progress().isDone()) {
            return true;
        }
        return false;
    }

    public CounterProgress bulkProgress() {
        ProgressTracker tracker = alterationProgress.get();
        if (tracker == null) {
            return null;
        }
        return tracker.progress();
    }

    public final boolean executeBulkOperation(AlternationAction<?>... actions) {
        ProgressTracker prev = alterationProgress.getAndUpdate((current) -> {
            if (current != null && current.progress() != null && !current.progress().isDone()) {
                return current;
            }
            ProgressTracker notifier = new ProgressTracker()
                .progress(storage.updateEach(AlternationAction.updaterFor(storage, actions), plugin.executor())).waitTimeout(25)
                .progressNotifier(alterationNotifier).doneNotifier(alterationNotifier);
            plugin.scheduler().async(notifier::await);
            return notifier;
        });
        return prev != alterationProgress.get();
    }

    public final MultiNotifier bulkNotifier() {
        return alterationNotifier;
    }

    public final IStorage storage() {
        return storage;
    }

    public final void updateConfiguration(MainConfig config) {
        if (config != null && storage instanceof CachedStorage cached) {
            cached.cache().cacheTime(player ? config.playerCacheKeepInMemory() : config.levelCacheKeepInMemory());
        }
    }

    @Override
    public void terminate() {
        if (storage instanceof CachedStorage cached) {
            tickTimer.remove(cached.cache());
        }
        storage.close();
    }

}
