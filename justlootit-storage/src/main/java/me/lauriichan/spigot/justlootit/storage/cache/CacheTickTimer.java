package me.lauriichan.spigot.justlootit.storage.cache;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import me.lauriichan.spigot.justlootit.storage.tick.AbstractTickTimer;

public final class CacheTickTimer extends AbstractTickTimer {

    private final ArrayList<Cache<?, ?>> caches = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public final void add(Cache<?, ?> cache) {
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

    public final boolean has(Cache<?, ?> cache) {
        lock.readLock().lock();
        try {
            return caches.contains(cache);
        } finally {
            lock.readLock().unlock();
        }
    }

    public final void remove(Cache<?, ?> cache) {
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
    protected void tick(long delta) {
        lock.readLock().lock();
        Cache<?, ?>[] caches;
        try {
            caches = this.caches.toArray(Cache[]::new);
        } finally {
            lock.readLock().unlock();
        }
        for (Cache<?, ?> cache : caches) {
            cache.tick();
        }
    }

}
