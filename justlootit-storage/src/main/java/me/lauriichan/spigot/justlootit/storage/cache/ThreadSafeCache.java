package me.lauriichan.spigot.justlootit.storage.cache;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ThreadSafeCache<K, V> extends Cache<K, V> {

    private final Cache<K, V> delegate;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    public ThreadSafeCache(Cache<K, V> delegate) {
        super(delegate.logger);
        this.delegate = delegate;
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
    protected boolean removeEntry(K key) {
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

}
