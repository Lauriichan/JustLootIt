package me.lauriichan.spigot.justlootit.capability;

import java.io.File;
import java.util.function.BiFunction;

import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.config.MainConfig;
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

public abstract class StorageCapability implements ICapability {

    static final class LevelContainerImpl extends StorageCapability {
        public static final MigrationSettings MIGRATION = new MigrationSettings(1024);
        public static final RAFSettings SETTINGS = RAFSettings.builder().migrationSupport(MIGRATION).build();

        public LevelContainerImpl(final VersionHandler handler, final LevelAdapter adapter) {
            super((JustLootItPlugin) handler.plugin(), (plugin, registry) -> new RAFMultiStorage(registry,
                new File(adapter.asBukkit().getWorldFolder(), "justlootit/containers"), SETTINGS), false, true);
        }

        public LevelContainerImpl(final VersionHandler handler, final ProtoWorld world) {
            super((JustLootItPlugin) handler.plugin(),
                (plugin, registry) -> new RAFMultiStorage(registry, new File(world.getWorldFolder(), "justlootit/containers"), SETTINGS),
                false, false);
        }
    }

    static final class PlayerImpl extends StorageCapability {
        public static final MigrationSettings MIGRATION = new MigrationSettings(64);
        public static final RAFSettings SETTINGS = RAFSettings.builder().migrationSupport(MIGRATION).copyBufferBytes(128).valuesPerFile(256)
            .build();

        public PlayerImpl(final VersionHandler handler, final PlayerAdapter adapter) {
            super((JustLootItPlugin) handler.plugin(),
                (plugin, registry) -> new RAFSingleStorage(registry,
                    new File(plugin.mainWorldFolder(), "justlootit/players/" + adapter.getUniqueId().toString() + ".jli"), SETTINGS),
                true, true);
        }
    }

    private final CacheTickTimer tickTimer;
    private final boolean player;

    protected final IStorage storage;

    public StorageCapability(final JustLootItPlugin plugin, final BiFunction<JustLootItPlugin, StorageAdapterRegistry, IStorage> creator,
        final boolean player, final boolean cached) {
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
