package me.lauriichan.spigot.justlootit.storage.util.cache;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.lauriichan.laylib.logger.ISimpleLogger;

public final class Long2ObjectMapCache<V> extends MapCache<Long, V> {

    private final Long2ObjectOpenHashMap<CachedValue<V>> map = new Long2ObjectOpenHashMap<>();

    public Long2ObjectMapCache(final ISimpleLogger logger) {
        super(logger);
    }

    public Long2ObjectMapCache(final ISimpleLogger logger, final ICallback<Long, V> callback) {
        super(logger, callback);
    }

    @Override
    protected int entryCount() {
        return map.size();
    }

    @Override
    protected boolean hasEntry(final Long key) {
        return map.containsKey(key.longValue());
    }

    @Override
    protected CachedValue<V> getEntry(final Long key) {
        return map.get(key.longValue());
    }

    @Override
    protected void putEntry(final Long key, final CachedValue<V> value) {
        map.put(key.longValue(), value);
    }

    @Override
    protected CachedValue<V> removeEntry(final Long key) {
        return map.remove(key.longValue());
    }

    @Override
    protected Long[] entryKeys() {
        return map.keySet().toArray(Long[]::new);
    }

    @Override
    protected boolean hasNoEntries() {
        return map.isEmpty();
    }

}
