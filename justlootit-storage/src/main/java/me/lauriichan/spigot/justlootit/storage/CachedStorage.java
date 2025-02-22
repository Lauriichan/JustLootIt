package me.lauriichan.spigot.justlootit.storage;

import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.storage.util.cache.Long2ObjectMapCache;
import me.lauriichan.spigot.justlootit.storage.util.counter.CounterProgress;

public class CachedStorage implements IStorage {

    private final IStorage delegate;
    private final Long2ObjectMapCache<Stored<?>> cache;

    private final ISimpleLogger logger;

    public CachedStorage(final IStorage delegate) {
        this.delegate = delegate;
        this.logger = delegate.logger();
        this.cache = new Long2ObjectMapCache<>(logger, this::invalidate);
    }

    private void invalidate(final Long key, final Stored<?> stored) {
        if (stored != null && !stored.isDirty()) {
            return;
        }
        if (stored != null) {
            stored.unsetDirty();
        }
        if (stored == null || stored.isEmpty()) {
            try {
                delegate.delete(key);
            } catch (final StorageException exp) {
                logger.warning("Couldn't delete resource with id '" + Long.toHexString(key) + "'!", exp);
            }
            return;
        }
        try {
            delegate.write(stored);
        } catch (final StorageException exp) {
            logger.warning("Couldn't save resource with id '" + Long.toHexString(key) + "'!", exp);
        }
    }

    public final Long2ObjectMapCache<Stored<?>> cache() {
        return cache;
    }

    @Override
    public ISimpleLogger logger() {
        return logger;
    }

    @Override
    public StorageAdapterRegistry registry() {
        return delegate.registry();
    }

    @Override
    public boolean isSupported(long id) {
        return delegate.isSupported(id);
    }

    @Override
    public boolean has(long id) throws StorageException {
        return cache.has(id) || delegate.has(id);
    }

    @Override
    public <T> Stored<T> read(long id) throws StorageException {
        Stored<?> stored = cache.get(id);
        if (stored == null) {
            stored = delegate.read(id);
            if (stored == null) {
                return null;
            }
            cache.set(stored.id(), stored);
        }
        return stored.cast();
    }

    @Override
    public void write(Stored<?> stored) throws StorageException {
        stored.setDirty();
        if (stored.needsId()) {
            delegate.write(stored);
            cache.set(stored.id(), stored);
            return;
        }
        Stored<?> other = cache.peek(stored.id());
        if (other == null) {
            cache.set(stored.id(), stored);
        }
    }

    @Override
    public boolean delete(long id) throws StorageException {
        final Stored<?> stored = cache.peek(id);
        if (stored != null) {
            cache.set(id, null);
        }
        return delegate.delete(id);
    }

    @Override
    public CounterProgress forEach(Consumer<Stored<?>> reader, Executor executor) {
        cache.clear();
        return delegate.forEach(reader, executor);
    }

    @Override
    public CounterProgress updateEach(Function<Stored<?>, UpdateInfo<?>> updater, Executor executor) {
        cache.clear();
        return delegate.updateEach(updater, executor);
    }

    @Override
    public void clear() throws StorageException {
        cache.clear();
        delegate.clear();
    }

    @Override
    public void close() throws StorageException {
        cache.clear();
        delegate.close();
    }

}
