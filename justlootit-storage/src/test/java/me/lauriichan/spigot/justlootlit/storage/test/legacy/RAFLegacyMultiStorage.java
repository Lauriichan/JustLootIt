package me.lauriichan.spigot.justlootlit.storage.test.legacy;

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
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.IRAFEntry;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.IRAFFile;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.RAFFileHelper;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.legacy.RAFFileLegacy;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.legacy.RAFSettingsLegacy;
import me.lauriichan.spigot.justlootit.storage.util.cache.Int2ObjectMapCache;
import me.lauriichan.spigot.justlootit.storage.util.cache.ThreadSafeMapCache;

public final class RAFLegacyMultiStorage extends Storage {

    private final RAFSettingsLegacy settings;

    private final File directory;
    private final ThreadSafeMapCache<Integer, IRAFFile> files;

    private final IIdentifier identifier;

    public RAFLegacyMultiStorage(final StorageAdapterRegistry registry, final File directory) {
        this(registry, directory, RAFSettingsLegacy.DEFAULT);
    }

    public RAFLegacyMultiStorage(final StorageAdapterRegistry registry, final File directory, final RAFSettingsLegacy settings) {
        super(registry);
        this.settings = settings;
        this.directory = directory;
        this.files = new ThreadSafeMapCache<>(new Int2ObjectMapCache<>(logger, this::decacheFile));
        createDirectory();
        this.identifier = new FileIdentifier(logger, directory);
    }

    private void createDirectory() {
        if (!directory.exists()) {
            directory.mkdirs();
        } else if (directory.isFile()) {
            directory.delete();
            directory.mkdirs();
        }
    }

    private long newId() {
        long id = identifier.nextId();
        while (has(id)) {
            id = identifier.nextId();
        }
        return id;
    }
    
    private IRAFFile create(int id) {
        return new RAFFileLegacy(settings, directory, id);
    }
    
    private void decacheFile(int fileId, IRAFFile file) {
        if (!file.isOpen()) {
            return;
        }
        file.close();
    }

    private void cacheFile(final IRAFFile file) {
        if (files.size() < settings.fileCacheMaxAmount) {
            files.set(file.id(), file);
            return;
        }
        long cacheTime = settings.fileCacheTicks;
        while (files.size() >= settings.fileCacheMaxAmount) {
            cacheTime -= settings.fileCachePurgeStep;
            files.purge(cacheTime);
        }
        files.set(file.id(), file);
    }

    @Override
    public boolean isSupported(long id) {
        return Long.compareUnsigned(id >> settings.valueIdBits | 0xFFFFFFFF, 0xFFFFFFFF) <= 0;
    }

    @Override
    public boolean has(long id) throws StorageException {
        final long possibleId = id >> settings.valueIdBits;
        if (Long.compareUnsigned(possibleId | 0xFFFFFFFF, 0xFFFFFFFF) >= 1) {
            return false;
        }
        final int fileId = (int) (possibleId & 0xFFFFFFFF);
        if (files.has(fileId)) {
            return files.get(fileId).has(id);
        }
        IRAFFile file = create(fileId);
        if (!file.isOpen()) {
            if (!file.exists()) {
                return false;
            }
            file.open();
        }
        cacheFile(file);
        return file.has(id);
    }

    @Override
    public <T> Stored<T> read(long id) throws StorageException {
        final long possibleId = id >> settings.valueIdBits;
        if (Long.compareUnsigned(possibleId | 0xFFFFFFFF, 0xFFFFFFFF) >= 1) {
            throw new StorageException("Unsupported file id '" + Long.toHexString(possibleId) + "'!");
        }
        final int fileId = (int) (possibleId & 0xFFFFFFFF);
        if (files.has(fileId)) {
            return read(files.get(fileId), id);
        }
        IRAFFile file = create(fileId);
        if (!file.isOpen()) {
            if (!file.exists()) {
                return null;
            }
            file.open();
        }
        cacheFile(file);
        return read(file, id);
    }

    private <T> Stored<T> read(IRAFFile file, long id) {
        if (!file.isOpen()) {
            files.remove(file.id());
            return null;
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
        } finally {
            if (!file.isOpen()) {
                files.remove(file.id());
            }
        }
    }

    @Override
    public void write(Stored<?> stored) throws StorageException {
        if (stored.needsId()) {
            stored.id(newId());
        }
        final long possibleId = stored.id() >> settings.valueIdBits;
        if (Long.compareUnsigned(possibleId | 0xFFFFFFFF, 0xFFFFFFFF) >= 1) {
            throw new StorageException("Unsupported file id '" + Long.toHexString(possibleId) + "'!");
        }
        final int fileId = (int) (possibleId & 0xFFFFFFFF);
        if (files.has(fileId)) {
            write(files.get(fileId), stored);
            return;
        }
        IRAFFile file = create(fileId);
        if (!file.isOpen()) {
            file.open();
        }
        cacheFile(file);
        write(file, stored);
    }

    private void write(IRAFFile file, Stored<?> stored) {
        try {
            ByteBuf buffer = Unpooled.buffer();
            stored.write(logger, buffer);
            file.write(RAFFileHelper.newEntry(stored.id(), stored.adapter().typeId(), stored.version(), buffer));
        } finally {
            if (!file.isOpen()) {
                files.remove(file.id());
            }
        }
    }

    @Override
    public boolean delete(long id) throws StorageException {
        final long possibleId = id >> settings.valueIdBits;
        if (Long.compareUnsigned(possibleId | 0xFFFFFFFF, 0xFFFFFFFF) >= 1) {
            throw new StorageException("Unsupported file id '" + Long.toHexString(possibleId) + "'!");
        }
        final int fileId = (int) (possibleId & 0xFFFFFFFF);
        if (files.has(fileId)) {
            return delete(files.get(fileId), id);
        }
        IRAFFile file = create(fileId);
        if (!file.isOpen()) {
            if (!file.exists()) {
                return false;
            }
            file.open();
        }
        cacheFile(file);
        return delete(file, id);
    }

    private boolean delete(IRAFFile file, long id) {
        try {
            return file.delete(id);
        } finally {
            if (!file.isOpen()) {
                files.remove(file.id());
            }
        }
    }

    @Override
    public void updateEach(Function<Stored<?>, UpdateInfo<?>> updater) {
        if (!directory.exists()) {
            return;
        }
        files.tickPaused(true);
        try {
            final File[] fileArray = directory.listFiles(RAFFileHelper.FILE_FILTER);
            for (final File file : fileArray) {
                int fileId;
                try {
                    fileId = Integer.parseInt(RAFFileHelper.getRAFFileName(file));
                } catch (NumberFormatException nfe) {
                    continue;
                }
                if (files.has(fileId)) {
                    IRAFFile rafFile = files.get(fileId);
                    try {
                        updateEach(rafFile, updater);
                    } catch(final StorageException exp) {
                        logger.warning("Failed to run update for file '{0}'!", exp, Integer.toHexString(fileId));
                    }
                    if (!rafFile.isOpen()) {
                        files.remove(fileId);
                    }
                    continue;
                }
                try (IRAFFile rafFile = create(fileId)) {
                    updateEach(rafFile, updater);
                } catch(StorageException exp) {
                    logger.warning("Failed to run update for file '{0}'!", exp, Integer.toHexString(fileId));
                }
            }
        } finally {
            files.tickPaused(false);
        }
    }

    private void updateEach(IRAFFile file, Function<Stored<?>, UpdateInfo<?>> updater) {
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
        files.tickPaused(true);
        try {
            files.clear();
            final File[] fileArray = directory.listFiles(RAFFileHelper.FILE_FILTER);
            for (final File file : fileArray) {
                try {
                    // Do this in order to only delete valid files
                    Integer.parseInt(RAFFileHelper.getRAFFileName(file));
                } catch (NumberFormatException nfe) {
                    continue;
                }
                file.delete();
            }
        } finally {
            files.tickPaused(false);
        }
        identifier.reset();
        identifier.save();
    }

    @Override
    public void close() throws StorageException {
        files.tickPaused(true);
        try {
            files.clear();
        } finally {
            files.tickPaused(false);
        }
        identifier.save();
    }

}