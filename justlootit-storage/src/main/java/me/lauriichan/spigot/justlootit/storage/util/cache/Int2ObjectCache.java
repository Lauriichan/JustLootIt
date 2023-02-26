package me.lauriichan.spigot.justlootit.storage.util.cache;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.lauriichan.laylib.logger.ISimpleLogger;

public final class Int2ObjectCache<V> extends Cache<Integer, V> {

    private final Int2ObjectOpenHashMap<CachedValue<V>> map = new Int2ObjectOpenHashMap<>();

    public Int2ObjectCache(ISimpleLogger logger) {
        super(logger);
    }

    public Int2ObjectCache(ISimpleLogger logger, ICacheCallback<Integer, V> callback) {
        super(logger, callback);
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
