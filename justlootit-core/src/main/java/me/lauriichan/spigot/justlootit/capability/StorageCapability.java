package me.lauriichan.spigot.justlootit.capability;

import java.io.File;

import me.lauriichan.spigot.justlootit.data.CacheLookupTable;
import me.lauriichan.spigot.justlootit.data.CachedInventory;
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

public abstract class StorageCapability implements ICapability {

    static final class LevelContainerImpl extends StorageCapability {

        public LevelContainerImpl(final VersionHandler handler, final LevelAdapter adapter) {
            super(new CachedStorage<>(new RAFMultiStorage<>(handler.logger(), Storable.class,
                new File(adapter.asBukkit().getWorldFolder(), "justlootit/containers"))));
            setupStorage();
        }
        
        public LevelContainerImpl(final VersionHandler handler, final ProtoWorld world) {
            super(new CachedStorage<>(new RAFMultiStorage<>(handler.logger(), Storable.class,
                new File(world.getWorldFolder(), "justlootit/containers"))));
            setupStorage();
        }
        
        private void setupStorage() {
            storage.register(VanillaContainer.ADAPTER);
            storage.register(StaticContainer.ADAPTER);
            storage.register(FrameContainer.ADAPTER);
        }

    }

    static final class PlayerImpl extends StorageCapability {

        public PlayerImpl(final VersionHandler handler, final PlayerAdapter adapter) {
            super(new CachedStorage<>(new RAFSingleStorage<>(handler.logger(), Storable.class,
                new File(handler.mainWorldFolder(), "justlootit/players/" + adapter.getUniqueId().toString() + ".jli"),
                RAFSettings.builder().copyBufferBytes(128).valuesPerFile(64).build())));
            storage.register(CachedInventory.ADAPTER);
            storage.register(CacheLookupTable.ADAPTER);
        }

    }

    protected final IStorage<Storable> storage;

    public StorageCapability(final IStorage<Storable> storage) {
        this.storage = storage;
    }

    public final IStorage<Storable> storage() {
        return storage;
    }

    @Override
    public void terminate() {
        storage.close();
    }

}
