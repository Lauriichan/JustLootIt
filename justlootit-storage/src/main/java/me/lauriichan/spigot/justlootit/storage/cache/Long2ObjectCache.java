package me.lauriichan.spigot.justlootit.storage.cache;

import java.util.logging.Logger;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public final class Long2ObjectCache<V> extends Cache<Long, V> {

    private final Long2ObjectOpenHashMap<CachedValue<V>> map = new Long2ObjectOpenHashMap<>();

    public Long2ObjectCache(Logger logger) {
        super(logger);
    }

    @Override
    protected boolean hasEntry(Long key) {
        return map.containsKey(key.longValue());
    }

    @Override
    protected CachedValue<V> getEntry(Long key) {
        return map.get(key.longValue());
    }

    @Override
    protected void putEntry(Long key, CachedValue<V> value) {
        map.put(key.longValue(), value);
    }

    @Override
    protected boolean removeEntry(Long key) {
        return map.remove(key.longValue()) != null;
    }

    @Override
    protected Long[] entryKeys() {
        return map.keySet().toArray(Long[]::new);
    }

}
