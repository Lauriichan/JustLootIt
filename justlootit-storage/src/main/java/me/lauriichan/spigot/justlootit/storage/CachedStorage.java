package me.lauriichan.spigot.justlootit.storage;

import java.util.function.Function;

import me.lauriichan.spigot.justlootit.storage.util.cache.Long2ObjectMapCache;

public class CachedStorage<S extends Storable> extends Storage<S> {

    private static final class CacheObject<S extends Storable> {

        private boolean dirty = false;
        private S storable;

        public CacheObject(S storable) {
            this.storable = storable;
        }

        public S storable() {
            return storable;
        }

        public CacheObject<S> storable(S storable) {
            this.dirty = true;
            this.storable = storable;
            return this;
        }

        public CacheObject<S> setDirty() {
            this.dirty = true;
            return this;
        }

        public boolean isDirty() {
            return dirty;
        }

    }

    private final Storage<S> delegate;
    private final Long2ObjectMapCache<CacheObject<S>> cache;

    public CachedStorage(Storage<S> delegate) {
        super(delegate.logger, delegate.baseType);
        this.delegate = delegate;
        this.cache = new Long2ObjectMapCache<>(logger, this::invalidate);
    }

    private void invalidate(Long key, CacheObject<S> cached) {
        S storable = cached.storable();
        if (!cached.isDirty() && (storable != null && storable instanceof IModifiable modifable && !modifable.isDirty())) {
            return;
        }
        if (storable == null) {
            try {
                delegate.delete(key.longValue());
            } catch (StorageException exp) {
                logger.warning("Couldn't delete resource with id '" + Long.toHexString(key) + "'!", exp);
            }
            return;
        }
        try {
            delegate.write(storable);
        } catch (StorageException exp) {
            logger.warning("Couldn't save resource with id '" + Long.toHexString(key) + "'!", exp);
        }
    }
    
    @Override
    public boolean isSupported(long id) {
        return delegate.isSupported(id);
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
    public boolean has(long id) throws StorageException {
        return cache.has(id) || delegate.has(id);
    }

    @Override
    public S read(long id) throws StorageException {
        CacheObject<S> cached = cache.get(id);
        if (cached != null) {
            return cached.storable();
        }
        S storable = delegate.read(id);
        if (storable == null) {
            return storable;
        }
        cache.set(storable.id(), new CacheObject<>(storable));
        return storable;
    }

    @Override
    public void write(S storable) throws StorageException {
        CacheObject<S> cached = cache.get(storable.id());
        if (cached != null) {
            cached.storable(storable);
            return;
        }
        cache.set(storable.id(), new CacheObject<>(storable).setDirty());
    }

    @Override
    public boolean delete(long id) throws StorageException {
        CacheObject<S> cached = cache.peek(id);
        if (cached != null) {
            cached.storable(null);
        }
        return delegate.delete(id);
    }

    @Override
    public void updateEach(Function<S, UpdateInfo<S>> updater) throws StorageException {
        cache.clear();
        updateEach(updater);
    }

}
