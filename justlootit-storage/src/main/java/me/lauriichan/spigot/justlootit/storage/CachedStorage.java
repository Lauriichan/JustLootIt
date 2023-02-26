package me.lauriichan.spigot.justlootit.storage;

import java.util.function.Consumer;

import me.lauriichan.spigot.justlootit.storage.util.cache.Long2ObjectCache;

public class CachedStorage<S extends Storable> extends Storage<S> {

    private final Storage<S> delegate;
    private final Long2ObjectCache<S> cache;

    public CachedStorage(Storage<S> delegate) {
        super(delegate.logger, delegate.baseType);
        this.delegate = delegate;
        this.cache = new Long2ObjectCache<>(logger);
    }

    @Override
    public void close() throws StorageException {
        cache.clear();
        delegate.close();
    }

    @Override
    public void clear() throws StorageException {
        cache.clear();
        delegate.clear();
    }

    @Override
    public S read(long id) throws StorageException {
        S storable = cache.get(id);
        if (storable != null) {
            return storable;
        }
        storable = delegate.read(id);
        if (storable == null) {
            return storable;
        }
        cache.set(storable.id(), storable);
        return storable;
    }

    @Override
    public void write(S storable) throws StorageException {
        write(storable);
        cache.set(storable.id(), storable);
    }

    @Override
    public boolean delete(long id) throws StorageException {
        cache.remove(id);
        return delegate.delete(id);
    }

    @Override
    public void updateEach(Consumer<S> updater) throws StorageException {
        cache.clear();
        updateEach(updater);
    }

}
