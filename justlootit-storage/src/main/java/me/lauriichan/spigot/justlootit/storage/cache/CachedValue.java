package me.lauriichan.spigot.justlootit.storage.cache;

public final class CachedValue<V> {

    private volatile V value;
    private volatile long time;

    public CachedValue(final V value) {
        this.value = value;
    }

    public V value() {
        time = 0;
        return value;
    }

    public void value(V value) {
        time = 0;
        this.value = value;
    }

    public long time() {
        return time;
    }

    public long tick() {
        time += 1;
        return time;
    }

}
