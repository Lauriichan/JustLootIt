package me.lauriichan.spigot.justlootit.capability;

import java.io.File;

import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.capability.ICapability;
import me.lauriichan.spigot.justlootit.storage.CachedStorage;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.storage.Storage;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.RAFMultiStorage;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.RAFSettings;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.RAFSingleStorage;

public abstract class StorageCapability<C> implements ICapability {

    static final class LevelImpl extends StorageCapability<LevelAdapter> {

        public LevelImpl(VersionHandler handler, LevelAdapter adapter) {
            super(new CachedStorage<>(new RAFMultiStorage<>(handler.logger(), Storable.class,
                new File(adapter.asBukkit().getWorldFolder(), "justlootit/containers"))));
        }

    }

    static final class PlayerImpl extends StorageCapability<PlayerAdapter> {

        public PlayerImpl(VersionHandler handler, PlayerAdapter adapter) {
            super(new CachedStorage<>(new RAFSingleStorage<>(handler.logger(), Storable.class,
                new File(handler.plugin().getDataFolder(), "player/" + adapter.getUniqueId().toString() + ".jli"),
                RAFSettings.builder().copyBufferBytes(128).valuesPerFile(64).build())));
        }

    }

    protected final Storage<Storable> storage;

    public StorageCapability(final Storage<Storable> storage) {
        this.storage = storage;
    }

    public final Storage<Storable> storage() {
        return storage;
    }

    @Override
    public void terminate() {
        storage.close();
    }

}
