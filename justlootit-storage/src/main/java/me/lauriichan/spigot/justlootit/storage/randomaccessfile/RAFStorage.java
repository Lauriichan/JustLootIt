package me.lauriichan.spigot.justlootit.storage.randomaccessfile;

import java.io.File;
import java.util.function.Consumer;

import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.storage.Storage;
import me.lauriichan.spigot.justlootit.storage.cache.Int2ObjectCache;
import me.lauriichan.spigot.justlootit.storage.cache.ThreadSafeCache;

public class RAFStorage<S extends Storable> extends Storage<S> {

    private final File directory;
    private final ThreadSafeCache<Integer, RAFAccess<S>> accesses = new ThreadSafeCache<>(new Int2ObjectCache<>());

    public RAFStorage(Class<S> baseType, File directory) {
        super(baseType);
        this.directory = directory;
    }

    @Override
    public void updateEach(Consumer<S> updater) {
        if (!directory.exists()) {
            return;
        }
        File[] files = directory.listFiles(RAFAccess.FILE_FILTER);
        for (File file : files) {
            int fileId;
            try {
                fileId = Integer.parseInt(file.getName().substring(0, file.getName().length() - 4));
            } catch (NumberFormatException nfe) {
                continue;
            }
            if (!accesses.has(fileId)) {
                // TODO: Load and update
            }
        }
    }

    @Override
    public void write(S storable) {
        long id = storable.id();
        long possibleId = id >> 10;
        if (Long.compareUnsigned((possibleId | 0xFFFFFFFF), 0xFFFFFFFF) >= 1) {
            throw new IllegalStateException("Unsupported file id '" + Long.toHexString(possibleId) + "'!");
        }
        int fileId = (int) (possibleId & 0xFFFFFFFF);
        short valueId = (short) (id & 0x3FF);
        if (accesses.has(fileId)) {
            accesses.get(fileId).store(valueId, storable);
            return;
        }
        RAFAccess<S> access = new RAFAccess<>(fileId, directory);
        accesses.set(fileId, access);
        access.store(valueId, storable);
    }

    @Override
    public S read(long id) {
        long possibleId = id >> 10;
        if (Long.compareUnsigned((possibleId | 0xFFFFFFFF), 0xFFFFFFFF) >= 1) {
            throw new IllegalStateException("Unsupported file id '" + Long.toHexString(possibleId) + "'!");
        }
        int fileId = (int) (possibleId & 0xFFFFFFFF);
        short valueId = (short) (id & 0x3FF);
        if (accesses.has(fileId)) {
            return accesses.get(fileId).read(valueId);
        }
        RAFAccess<S> access = new RAFAccess<>(fileId, directory);
        accesses.set(fileId, access);
        return access.read(valueId);
    }

}
