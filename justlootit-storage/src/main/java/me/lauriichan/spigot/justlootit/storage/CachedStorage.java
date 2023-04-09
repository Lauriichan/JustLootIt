package me.lauriichan.spigot.justlootit.storage;

import java.util.function.Function;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.storage.util.cache.Long2ObjectMapCache;

public class CachedStorage<S extends Storable> implements IStorage<S> {

    private static final class CacheObject<S extends Storable> {

        private boolean dirty = false;
        private S storable;

        public CacheObject(final S storable) {
            this.storable = storable;
        }

        public S storable() {
            return storable;
        }

        public CacheObject<S> storable(final S storable) {
            this.dirty = true;
            this.storable = storable;
            return this;
        }

        public boolean isDirty() {
            return dirty;
        }

    }

    private final IStorage<S> delegate;
    private final Long2ObjectMapCache<CacheObject<S>> cache;

    private final ISimpleLogger logger;

    public CachedStorage(final IStorage<S> delegate) {
        this.delegate = delegate;
        this.logger = delegate.logger();
        this.cache = new Long2ObjectMapCache<>(logger, this::invalidate);
    }

    private void invalidate(final Long key, final CacheObject<S> cached) {
        final S storable = cached.storable();
        if (!cached.isDirty() && storable != null && storable instanceof final IModifiable modifable && !modifable.isDirty()) {
            return;
        }
        if (storable == null) {
            try {
                delegate.delete(key);
            } catch (final StorageException exp) {
                logger.warning("Couldn't delete resource with id '" + Long.toHexString(key) + "'!", exp);
            }
            return;
        }
        try {
            delegate.write(storable);
        } catch (final StorageException exp) {
            logger.warning("Couldn't save resource with id '" + Long.toHexString(key) + "'!", exp);
        }
    }

    @Override
    public ISimpleLogger logger() {
        return delegate.logger();
    }

    @Override
    public Class<S> baseType() {
        return delegate.baseType();
    }

    @Override
    public void register(final StorageAdapter<? extends S> adapter) throws StorageException {
        delegate.register(adapter);
    }

    @Override
    public boolean unregister(final Class<? extends S> type) {
        return delegate.unregister(type);
    }

    @Override
    public StorageAdapter<? extends S> findAdapterFor(final Class<? extends S> type) {
        return delegate.findAdapterFor(type);
    }

    @Override
    public StorageAdapter<? extends S> findAdapterFor(final short typeId) {
        return delegate.findAdapterFor(typeId);
    }

    @Override
    public boolean isSupported(final long id) {
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
    public boolean has(final long id) throws StorageException {
        return cache.has(id) || delegate.has(id);
    }

    @Override
    public S read(final long id) throws StorageException {
        final CacheObject<S> cached = cache.get(id);
        if (cached != null) {
            return cached.storable();
        }
        final S storable = delegate.read(id);
        if (storable == null) {
            return storable;
        }
        cache.set(storable.id(), new CacheObject<>(storable));
        return storable;
    }

    @Override
    public void write(final S storable) throws StorageException {
        final CacheObject<S> cached = cache.get(storable.id());
        if (cached != null) {
            cached.storable(storable);
            return;
        }
        cache.set(storable.id(), new CacheObject<>(storable));
        // Do write on first save
        delegate.write(storable);
    }

    @Override
    public boolean delete(final long id) throws StorageException {
        final CacheObject<S> cached = cache.peek(id);
        if (cached != null) {
            cached.storable(null);
        }
        return delegate.delete(id);
    }

    @Override
    public void updateEach(final Function<S, UpdateInfo<S>> updater) throws StorageException {
        cache.clear();
        updateEach(updater);
    }
    
    @Override
    public long newId() {
        return delegate.newId();
    }

}
