package me.lauriichan.spigot.justlootit.nms.capability;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class CapabilityManager {

    private final ObjectArrayList<ICapabilityProvider> list = new ObjectArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void forEach(final Consumer<ICapabilityProvider> consumer) {
        lock.readLock().lock();
        try {
            list.forEach(consumer);
        } finally {
            lock.readLock().unlock();
        }
    }

    public ICapabilityProvider[] array() {
        lock.readLock().lock();
        try {
            return list.toArray(ICapabilityProvider[]::new);
        } finally {
            lock.readLock().unlock();
        }
    }

    public ICapabilityProvider get(final int index) {
        lock.readLock().lock();
        try {
            return list.get(index);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean add(final ICapabilityProvider provider) {
        lock.readLock().lock();
        try {
            if (list.contains(provider)) {
                return false;
            }
        } finally {
            lock.readLock().unlock();
        }
        lock.writeLock().lock();
        try {
            return list.add(provider);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean remove(final ICapabilityProvider provider) {
        lock.writeLock().lock();
        try {
            return list.remove(provider);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int amount() {
        lock.readLock().lock();
        try {
            return list.size();
        } finally {
            lock.readLock().unlock();
        }
    }

}
