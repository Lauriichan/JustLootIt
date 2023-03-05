package me.lauriichan.spigot.justlootit.storage.util.cache;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ThreadSafeMapCache<K, V> extends MapCache<K, V> {

    private final MapCache<K, V> delegate;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    public ThreadSafeMapCache(MapCache<K, V> delegate) {
        super(delegate.logger);
        this.delegate = delegate;
    }
    
    @Override
    protected int entryCount() {
        readLock.lock();
        try {
            return delegate.entryCount();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    protected boolean hasEntry(K key) {
        readLock.lock();
        try {
            return delegate.hasEntry(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    protected CachedValue<V> getEntry(K key) {
        readLock.lock();
        try {
            return delegate.getEntry(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    protected void putEntry(K key, CachedValue<V> value) {
        writeLock.lock();
        try {
            delegate.putEntry(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    protected CachedValue<V> removeEntry(K key) {
        writeLock.lock();
        try {
            return delegate.removeEntry(key);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    protected K[] entryKeys() {
        readLock.lock();
        try {
            return delegate.entryKeys();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    protected boolean hasNoEntries() {
        readLock.lock();
        try {
            return delegate.hasNoEntries();
        } finally {
            readLock.unlock();
        }
    }

}
