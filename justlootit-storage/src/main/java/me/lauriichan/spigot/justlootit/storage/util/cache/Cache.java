package me.lauriichan.spigot.justlootit.storage.util.cache;

import java.util.Arrays;
import java.util.List;

import me.lauriichan.laylib.logger.ISimpleLogger;

public abstract class Cache<K, V> {

    @SuppressWarnings("rawtypes")
    private static final ICacheCallback NOP_CALLBACK = (a, b) -> {
    };

    @SuppressWarnings("unchecked")
    public static <A, B> ICacheCallback<A, B> nopCallback() {
        return NOP_CALLBACK;
    }

    protected final ISimpleLogger logger;
    protected final ICacheCallback<K, V> callback;
    
    private volatile long cacheTime = 1;
    private volatile boolean tickPaused = false;

    public Cache(final ISimpleLogger logger) {
        this(logger, nopCallback());
    }

    public Cache(final ISimpleLogger logger, ICacheCallback<K, V> callback) {
        this.logger = logger;
        this.callback = callback;
    }
    
    protected abstract boolean hasNoEntries();

    protected abstract boolean hasEntry(K key);

    protected abstract CachedValue<V> getEntry(K key);

    protected abstract void putEntry(K key, CachedValue<V> value);

    protected abstract CachedValue<V> removeEntry(K key);

    protected abstract K[] entryKeys();
    
    public final void tickPaused(boolean tickPaused) {
        this.tickPaused = tickPaused;
    }
    
    public final boolean tickPaused() {
        return tickPaused;
    }

    public final void cacheTime(long cacheTime) {
        this.cacheTime = Math.max(cacheTime, 1);
    }

    public final long cacheTime() {
        return cacheTime;
    }

    public final V get(K key) {
        CachedValue<V> entry = getEntry(key);
        if (entry == null) {
            return null;
        }
        return entry.value();
    }

    public final V peek(K key) {
        CachedValue<V> entry = getEntry(key);
        if (entry == null) {
            return null;
        }
        return entry.peekValue();
    }

    public final V remove(K key) {
        CachedValue<V> cached = removeEntry(key);
        if (cached != null) {
            return cached.value();
        }
        return null;
    }

    public final void set(K key, V value) {
        CachedValue<V> cached = getEntry(key);
        if (cached != null) {
            cached.value(value);
            return;
        }
        putEntry(key, new CachedValue<>(value));
    }

    public final boolean has(K key) {
        return hasEntry(key);
    }

    public final List<K> keys() {
        return Arrays.asList(entryKeys());
    }

    public final void clear() {
        List<K> keys = keys();
        for (K key : keys) {
            CachedValue<V> entry = removeEntry(key);
            if (entry == null) { // Value is already uncached
                continue;
            }
            try {
                callback.onRemove(key, entry.peekValue());
            } catch (Exception e) {
                logger.warning("Couldn't run remove callback for resource", e);
            }
        }
    }
    
    public final boolean isEmpty() {
        return hasNoEntries();
    }

    void tick() {
        if(tickPaused || !hasNoEntries()) {
            return;
        }
        K[] keys = entryKeys();
        for (K key : keys) {
            CachedValue<V> entry = getEntry(key);
            if (entry.tick() < cacheTime) {
                continue;
            }
            removeEntry(key);
            V value = entry.value();
            try {
                callback.onInvalidate(key, entry.peekValue());
            } catch(Exception e) {
                logger.warning("Couldn't run invalidate callback for resource", e);
            }
            if (value instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) value).close();
                } catch (Exception e) {
                    logger.warning("Couldn't close cached resource", e);
                }
            }
        }
    }

}