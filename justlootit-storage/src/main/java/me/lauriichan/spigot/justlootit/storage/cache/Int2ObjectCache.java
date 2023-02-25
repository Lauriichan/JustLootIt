package me.lauriichan.spigot.justlootit.storage.cache;

import java.util.logging.Logger;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public final class Int2ObjectCache<V> extends Cache<Integer, V> {

    private final Int2ObjectOpenHashMap<CachedValue<V>> map = new Int2ObjectOpenHashMap<>();

    public Int2ObjectCache(Logger logger) {
        super(logger);
    }

    @Override
    protected boolean hasEntry(Integer key) {
        return map.containsKey(key.intValue());
    }

    @Override
    protected CachedValue<V> getEntry(Integer key) {
        return map.get(key.intValue());
    }

    @Override
    protected void putEntry(Integer key, CachedValue<V> value) {
        map.put(key.intValue(), value);
    }

    @Override
    protected CachedValue<V> removeEntry(Integer key) {
        return map.remove(key.intValue());
    }
    
    @Override
    protected Integer[] entryKeys() {
        return map.keySet().toArray(Integer[]::new);
    }

}
