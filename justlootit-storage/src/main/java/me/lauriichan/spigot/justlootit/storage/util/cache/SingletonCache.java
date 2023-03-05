package me.lauriichan.spigot.justlootit.storage.util.cache;

import me.lauriichan.laylib.logger.ISimpleLogger;

public abstract class SingletonCache<V> extends Cache {

    public static interface ICallback<V> {

        default void onInvalidate(V value) throws Exception {
            onRemove(value);
        }

        void onRemove(V value) throws Exception;

    }

    @SuppressWarnings("rawtypes")
    private static final ICallback NOP_CALLBACK = (a) -> {
    };

    @SuppressWarnings("unchecked")
    public static <A> ICallback<A> nopCallback() {
        return NOP_CALLBACK;
    }

    protected final ISimpleLogger logger;
    protected final ICallback<V> callback;

    public SingletonCache(ISimpleLogger logger) {
        this(logger, nopCallback());
    }

    public SingletonCache(ISimpleLogger logger, ICallback<V> callback) {
        this.logger = logger;
        this.callback = callback;
    }

    protected abstract boolean hasValue();

    protected abstract void setValue(CachedValue<V> value);

    protected abstract CachedValue<V> getValue();

    protected abstract void clearValue();

    public final V get() {
        CachedValue<V> cached = getValue();
        if (cached != null) {
            return cached.value();
        }
        return null;
    }

    public final V peek() {
        CachedValue<V> cached = getValue();
        if (cached != null) {
            return cached.peekValue();
        }
        return null;
    }
    
    public final void set(V value) {
        CachedValue<V> cached = getValue();
        if(cached == null) {
            setValue(new CachedValue<>(value));
            return;
        }
        cached.value(value);
    }

    public final V remove() {
        CachedValue<V> cached = getValue();
        if (cached == null) {
            return null;
        }
        clearValue();
        V value = cached.peekValue();
        try {
            callback.onRemove(value);
        } catch (Exception e) {
            logger.warning("Couldn't run remove callback for resource", e);
        }
        return value;
    }

    public final boolean isEmpty() {
        return !hasValue();
    }

    public final boolean isPresent() {
        return hasValue();
    }

    @Override
    protected boolean canTick() {
        return hasValue();
    }

    @Override
    protected void onTick(long cacheTime) {
        CachedValue<V> cached = getValue();
        if (cached.tick() < cacheTime) {
            return;
        }
        clearValue();
        V value = cached.peekValue();
        try {
            callback.onInvalidate(value);
        } catch (Exception e) {
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
