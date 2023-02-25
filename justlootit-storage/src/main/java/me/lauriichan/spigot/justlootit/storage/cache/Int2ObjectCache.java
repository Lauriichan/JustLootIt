package me.lauriichan.spigot.justlootit.storage.cache;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public final class Int2ObjectCache<V> extends Cache<Integer, V> {

    private final Int2ObjectOpenHashMap<CachedValue<V>> map = new Int2ObjectOpenHashMap<>();

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
    protected boolean removeEntry(Integer key) {
        return map.remove(key.intValue()) != null;
    }
    
    @Override
    protected Integer[] entryKeys() {
        return map.keySet().toArray(Integer[]::new);
    }

}
