package me.lauriichan.spigot.justlootit.capability;

import java.io.File;

import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.config.MainConfig;
import me.lauriichan.spigot.justlootit.data.CacheLookupTable;
import me.lauriichan.spigot.justlootit.data.CachedInventory;
import me.lauriichan.spigot.justlootit.data.CompatibilityContainer;
import me.lauriichan.spigot.justlootit.data.FrameContainer;
import me.lauriichan.spigot.justlootit.data.StaticContainer;
import me.lauriichan.spigot.justlootit.data.VanillaContainer;
import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.capability.ICapability;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoWorld;
import me.lauriichan.spigot.justlootit.storage.CachedStorage;
import me.lauriichan.spigot.justlootit.storage.IStorage;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.RAFMultiStorage;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.RAFSettings;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.RAFSingleStorage;
import me.lauriichan.spigot.justlootit.storage.util.cache.CacheTickTimer;

public abstract class StorageCapability implements ICapability {

    static final class LevelContainerImpl extends StorageCapability {

        public LevelContainerImpl(final VersionHandler handler, final LevelAdapter adapter) {
            super((JustLootItPlugin) handler.plugin(), new CachedStorage<>(new RAFMultiStorage<>(handler.logger(), Storable.class,
                new File(adapter.asBukkit().getWorldFolder(), "justlootit/containers"))), false);
            setupStorage();
        }

        public LevelContainerImpl(final VersionHandler handler, final ProtoWorld world) {
            super((JustLootItPlugin) handler.plugin(),
                new RAFMultiStorage<>(handler.logger(), Storable.class, new File(world.getWorldFolder(), "justlootit/containers")), false);
            setupStorage();
        }

        private void setupStorage() {
            storage.register(CompatibilityContainer.ADAPTER);
            storage.register(VanillaContainer.ADAPTER);
            storage.register(StaticContainer.ADAPTER);
            storage.register(FrameContainer.ADAPTER);
        }

    }

    static final class PlayerImpl extends StorageCapability {

        public PlayerImpl(final VersionHandler handler, final PlayerAdapter adapter) {
            super((JustLootItPlugin) handler.plugin(),
                new CachedStorage<>(new RAFSingleStorage<>(handler.logger(), Storable.class,
                    new File(handler.mainWorldFolder(), "justlootit/players/" + adapter.getUniqueId().toString() + ".jli"),
                    RAFSettings.builder().copyBufferBytes(128).valuesPerFile(64).build())),
                true);
            storage.register(CachedInventory.ADAPTER);
            storage.register(CacheLookupTable.ADAPTER);
        }

    }

    private final CacheTickTimer tickTimer;
    private final boolean player;
    
    protected final IStorage<Storable> storage;

    public StorageCapability(final JustLootItPlugin plugin, final IStorage<Storable> storage, final boolean player) {
        this.tickTimer = player ? plugin.playerTickTimer() : plugin.levelTickTimer();
        this.player = player;
        this.storage = storage;
        if (storage instanceof CachedStorage<?> cached) {
            updateConfiguration(plugin.configManager().config(MainConfig.class));
            tickTimer.add(cached.cache());
        }
    }

    public final IStorage<Storable> storage() {
        return storage;
    }
    
    public final void updateConfiguration(MainConfig config) {
        if (config != null && storage instanceof CachedStorage<?> cached) {
            cached.cache().cacheTime(player ? config.playerCacheKeepInMemory() : config.levelCacheKeepInMemory());
        }
    }

    @Override
    public void terminate() {
        if (storage instanceof CachedStorage<?> cached) {
            tickTimer.remove(cached.cache());
        }
        storage.close();
    }

}
