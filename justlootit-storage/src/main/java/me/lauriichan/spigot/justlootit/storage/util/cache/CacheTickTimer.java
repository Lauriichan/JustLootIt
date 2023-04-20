package me.lauriichan.spigot.justlootit.storage.util.cache;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import me.lauriichan.spigot.justlootit.storage.util.tick.AbstractTickTimer;

public final class CacheTickTimer extends AbstractTickTimer {

    private final ArrayList<Cache> caches = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void add(final Cache cache) {
        lock.readLock().lock();
        try {
            if (caches.contains(cache)) {
                return;
            }
        } finally {
            lock.readLock().unlock();
        }
        lock.writeLock().lock();
        try {
            caches.add(cache);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean has(final Cache cache) {
        lock.readLock().lock();
        try {
            return caches.contains(cache);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void remove(final Cache cache) {
        lock.readLock().lock();
        try {
            if (!caches.contains(cache)) {
                return;
            }
        } finally {
            lock.readLock().unlock();
        }
        lock.writeLock().lock();
        try {
            caches.add(cache);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    protected void tick(final long delta) {
        lock.readLock().lock();
        Cache[] caches;
        try {
            caches = this.caches.toArray(MapCache[]::new);
        } finally {
            lock.readLock().unlock();
        }
        for (final Cache cache : caches) {
            cache.tick();
        }
    }

}
