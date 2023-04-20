package me.lauriichan.spigot.justlootit.storage.util.cache;

import me.lauriichan.laylib.logger.ISimpleLogger;

public final class ThreadSafeSingletonCache<V> extends SingletonCache<V> {

    private volatile CachedValue<V> value;

    public ThreadSafeSingletonCache(final ISimpleLogger logger) {
        super(logger);
    }

    public ThreadSafeSingletonCache(final ISimpleLogger logger, final ICallback<V> callback) {
        super(logger, callback);
    }

    @Override
    protected boolean hasValue() {
        return value != null;
    }

    @Override
    protected void setValue(final CachedValue<V> value) {
        this.value = value;
    }

    @Override
    protected CachedValue<V> getValue() {
        return value;
    }

    @Override
    protected void clearValue() {
        this.value = null;
    }

}
