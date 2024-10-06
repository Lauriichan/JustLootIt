package me.lauriichan.spigot.justlootit.storage.randomaccessfile;

import java.io.File;
import java.util.function.Function;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.lauriichan.spigot.justlootit.storage.Storage;
import me.lauriichan.spigot.justlootit.storage.StorageAdapterRegistry;
import me.lauriichan.spigot.justlootit.storage.StorageException;
import me.lauriichan.spigot.justlootit.storage.Stored;
import me.lauriichan.spigot.justlootit.storage.UpdateInfo;
import me.lauriichan.spigot.justlootit.storage.UpdateInfo.UpdateState;
import me.lauriichan.spigot.justlootit.storage.identifier.FileIdentifier;
import me.lauriichan.spigot.justlootit.storage.identifier.IIdentifier;

public final class RAFSingleStorage extends Storage {

    private final IRAFFile file;

    private final IIdentifier identifier;

    public RAFSingleStorage(final StorageAdapterRegistry registry, final File file) {
        super(registry);
        this.file = RAFFileHelper.create(registry, file);
        this.identifier = new FileIdentifier(logger, file);
    }

    private long newId() {
        long id = identifier.nextId();
        while (has(id)) {
            id = identifier.nextId();
        }
        return id;
    }

    @Override
    public boolean isSupported(long id) {
        return id < file.settings().valueIdAmount() && id >= 0;
    }

    @Override
    public boolean has(long id) throws StorageException {
        if (!file.isOpen()) {
            if (!file.exists()) {
                return false;
            }
            file.open();
        }
        return file.has(id);
    }

    @Override
    public <T> Stored<T> read(long id) throws StorageException {
        if (!file.isOpen()) {
            if (!file.exists()) {
                return null;
            }
            file.open();
        }
        IRAFEntry entry = file.read(id);
        if (entry == null) {
            return null;
        }
        try {
            Stored<T> stored = registry.create(entry.typeId());
            stored.id(id);
            stored.read(logger, entry.buffer());
            return stored;
        } catch (IllegalArgumentException iae) {
            delete(id);
            throw new StorageException("Failed to read item", iae);
        }
    }

    @Override
    public void write(Stored<?> stored) throws StorageException {
        if (stored.needsId()) {
            stored.id(newId());
        }
        ByteBuf buffer = Unpooled.buffer();
        stored.write(logger, buffer);
        IRAFEntry entry = RAFFileHelper.newEntry(stored.id(), stored.adapter().typeId(), stored.version(), buffer);
        if (!file.isOpen()) {
            file.open();
        }
        file.write(entry);
    }

    @Override
    public boolean delete(long id) throws StorageException {
        if (!file.isOpen()) {
            if (!file.exists()) {
                return false;
            }
            file.open();
        }
        return file.delete(id);
    }

    @Override
    public void updateEach(Function<Stored<?>, UpdateInfo<?>> updater) {
        if (!file.isOpen()) {
            if (!file.exists()) {
                return;
            }
            file.open();
        }
        file.modifyEach(entry -> {
            try {
                Stored<?> stored;
                try {
                    stored = registry.create(entry.typeId());
                    stored.id(entry.id());
                    stored.read(logger, entry.buffer());
                } catch (IllegalArgumentException iae) {
                    return null;
                }
                UpdateInfo<?> info = updater.apply(stored);
                if (info.state() == UpdateState.NONE) {
                    return entry;
                }
                if (info.state() == UpdateState.DELETE) {
                    return null;
                }
                ByteBuf buffer = Unpooled.buffer();
                stored.write(logger, buffer);
                return RAFFileHelper.newEntry(entry.id(), stored.adapter().typeId(), stored.version(), buffer);
            } catch (RuntimeException exp) {
                logger.warning("Failed to modify entry '{0}'", entry.id());
                return entry;
            }
        });
    }

    @Override
    public void clear() throws StorageException {
        if (file.isOpen()) {
            file.close();
        }
        if (file.exists()) {
            file.file().delete();
        }
        identifier.reset();
        identifier.save();
    }

    @Override
    public void close() throws StorageException {
        if (file.isOpen()) {
            file.close();
        }
        identifier.save();
    }

}
