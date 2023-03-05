package me.lauriichan.spigot.justlootit.nms.capability;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class CapabilityManager {

    private final ObjectArrayList<ICapabilityProvider> list = new ObjectArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public final ICapabilityProvider get(int index) {
        lock.readLock().lock();
        try {
            return list.get(index);
        } finally {
            lock.readLock().unlock();
        }
    }

    public final boolean add(ICapabilityProvider provider) {
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

    public final boolean remove(ICapabilityProvider provider) {
        lock.writeLock().lock();
        try {
            return list.remove(provider);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public final int amount() {
        lock.readLock().lock();
        try {
            return list.size();
        } finally {
            lock.readLock().unlock();
        }
    }

}
