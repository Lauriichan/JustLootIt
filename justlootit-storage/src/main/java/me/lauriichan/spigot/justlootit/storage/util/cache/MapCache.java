package me.lauriichan.spigot.justlootit.storage.util.cache;

import java.util.Arrays;
import java.util.List;

import me.lauriichan.laylib.logger.ISimpleLogger;

public abstract class MapCache<K, V> extends Cache {

    public interface ICallback<K, V> {

        default void onInvalidate(final K key, final V value) throws Exception {
            onRemove(key, value);
        }

        void onRemove(K key, V value) throws Exception;

    }

    @SuppressWarnings("rawtypes")
    private static final ICallback NOP_CALLBACK = (a, b) -> {
    };

    @SuppressWarnings("unchecked")
    public static <A, B> ICallback<A, B> nopCallback() {
        return NOP_CALLBACK;
    }

    protected final ISimpleLogger logger;
    protected final ICallback<K, V> callback;

    public MapCache(final ISimpleLogger logger) {
        this(logger, nopCallback());
    }

    public MapCache(final ISimpleLogger logger, final ICallback<K, V> callback) {
        this.logger = logger;
        this.callback = callback;
    }

    protected abstract boolean hasNoEntries();

    protected abstract int entryCount();

    protected abstract boolean hasEntry(K key);

    protected abstract CachedValue<V> getEntry(K key);

    protected abstract void putEntry(K key, CachedValue<V> value);

    protected abstract CachedValue<V> removeEntry(K key);

    protected abstract K[] entryKeys();

    public final int size() {
        return entryCount();
    }

    public final V get(final K key) {
        final CachedValue<V> entry = getEntry(key);
        if (entry == null) {
            return null;
        }
        return entry.value();
    }

    public final V peek(final K key) {
        final CachedValue<V> entry = getEntry(key);
        if (entry == null) {
            return null;
        }
        return entry.peekValue();
    }

    public final V remove(final K key) {
        final CachedValue<V> cached = removeEntry(key);
        if (cached != null) {
            return cached.value();
        }
        return null;
    }

    public final void set(final K key, final V value) {
        final CachedValue<V> cached = getEntry(key);
        if (cached != null) {
            cached.value(value);
            return;
        }
        putEntry(key, new CachedValue<>(value));
    }

    public final boolean has(final K key) {
        return hasEntry(key);
    }

    public final List<K> keys() {
        return Arrays.asList(entryKeys());
    }

    public final void clear() {
        final List<K> keys = keys();
        for (final K key : keys) {
            final CachedValue<V> entry = removeEntry(key);
            if (entry == null) { // Value is already uncached
                continue;
            }
            try {
                callback.onRemove(key, entry.peekValue());
            } catch (final Exception e) {
                logger.warning("Couldn't run remove callback for resource", e);
            }
        }
    }

    public final boolean isEmpty() {
        return hasNoEntries();
    }

    public final int purge(final long cacheTime) {
        if (hasNoEntries()) {
            return 0;
        }
        final K[] keys = entryKeys();
        int purged = 0;
        for (final K key : keys) {
            final CachedValue<V> entry = getEntry(key);
            if (entry == null || entry.time() < cacheTime) {
                continue;
            }
            purged++;
            removeEntry(key);
            final V value = entry.value();
            try {
                callback.onInvalidate(key, value);
            } catch (final Exception e) {
                logger.warning("Couldn't run invalidate callback for resource", e);
            }
            if (value instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) value).close();
                } catch (final Exception e) {
                    logger.warning("Couldn't close cached resource", e);
                }
            }
        }
        return purged;
    }

    @Override
    protected final boolean canTick() {
        return !hasNoEntries();
    }

    @Override
    protected final void onTick(final long cacheTime) {
        final K[] keys = entryKeys();
        for (final K key : keys) {
            final CachedValue<V> entry = getEntry(key);
            if (entry == null || entry.tick() < cacheTime) {
                continue;
            }
            removeEntry(key);
            final V value = entry.value();
            try {
                callback.onInvalidate(key, entry.peekValue());
            } catch (final Exception e) {
                logger.warning("Couldn't run invalidate callback for resource", e);
            }
            if (value instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) value).close();
                } catch (final Exception e) {
                    logger.warning("Couldn't close cached resource", e);
                }
            }
        }
    }

}
