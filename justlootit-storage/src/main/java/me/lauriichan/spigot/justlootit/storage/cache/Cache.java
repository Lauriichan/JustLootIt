package me.lauriichan.spigot.justlootit.storage.cache;

import java.util.Arrays;
import java.util.List;

public abstract class Cache<K, V> {

    protected volatile long cacheTime;

    public final void setCacheTime(long cacheTime) {
        this.cacheTime = Math.max(cacheTime, 1);
    }

    public final long getCacheTime() {
        return cacheTime;
    }

    protected abstract boolean hasEntry(K key);

    protected abstract CachedValue<V> getEntry(K key);

    protected abstract void putEntry(K key, CachedValue<V> value);

    protected abstract boolean removeEntry(K key);

    protected abstract K[] entryKeys();

    public final V get(K key) {
        CachedValue<V> entry = getEntry(key);
        if (entry == null) {
            return null;
        }
        return entry.value();
    }

    public final boolean remove(K key) {
        return removeEntry(key);
    }

    public final void set(K key, V value) {
        CachedValue<V> cached = getEntry(key);
        if (cached != null) {
            cached.value(value);
            return;
        }
        putEntry(key, new CachedValue<>(value));
    }

    public final List<K> keys() {
        return Arrays.asList(entryKeys());
    }

    void tick() {
        K[] keys = entryKeys();
        for (K key : keys) {
            CachedValue<V> entry = getEntry(key);
            if (entry.tick() >= cacheTime) {
                removeEntry(key);
            }
        }
    }

}
