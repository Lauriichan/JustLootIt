package me.lauriichan.spigot.justlootit.storage.util.cache;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.lauriichan.laylib.logger.ISimpleLogger;

public final class Int2ObjectMapCache<V> extends MapCache<Integer, V> {

    private final Int2ObjectOpenHashMap<CachedValue<V>> map = new Int2ObjectOpenHashMap<>();

    public Int2ObjectMapCache(final ISimpleLogger logger) {
        super(logger);
    }

    public Int2ObjectMapCache(final ISimpleLogger logger, final ICallback<Integer, V> callback) {
        super(logger, callback);
    }

    @Override
    protected int entryCount() {
        return map.size();
    }

    @Override
    protected boolean hasEntry(final Integer key) {
        return map.containsKey(key.intValue());
    }

    @Override
    protected CachedValue<V> getEntry(final Integer key) {
        return map.get(key);
    }

    @Override
    protected void putEntry(final Integer key, final CachedValue<V> value) {
        map.put(key, value);
    }

    @Override
    protected CachedValue<V> removeEntry(final Integer key) {
        return map.remove(key);
    }

    @Override
    protected Integer[] entryKeys() {
        return map.keySet().toArray(Integer[]::new);
    }

    @Override
    protected boolean hasNoEntries() {
        return map.isEmpty();
    }

}
