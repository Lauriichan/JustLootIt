package me.lauriichan.spigot.justlootit.storage;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public final class StorageAdapterRegistry {

    private final Object2ObjectOpenHashMap<Class<?>, StorageAdapter<?>> classToAdapter = new Object2ObjectOpenHashMap<>();
    private final Short2ObjectOpenHashMap<StorageAdapter<?>> idToAdapter = new Short2ObjectOpenHashMap<>();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final StorageMigrator migrator;

    public StorageAdapterRegistry(final StorageMigrator migrator) {
        this.migrator = migrator;
    }

    public final StorageMigrator migrator() {
        return migrator;
    }

    public final void register(final StorageAdapter<?> adapter) throws StorageException {
        lock.readLock().lock();
        try {
            if (classToAdapter.containsKey(adapter.type())) {
                throw new StorageException("There is already an adapter for the type '" + adapter.type().getName() + "'!");
            }
            if (idToAdapter.containsKey((short) adapter.typeId())) {
                throw new StorageException("There is already an adapter with the type id " + adapter.typeId() + "!");
            }
        } finally {
            lock.readLock().unlock();
        }
        lock.writeLock().lock();
        try {
            idToAdapter.put((short) adapter.typeId(), adapter);
            classToAdapter.put(adapter.type(), adapter);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public final boolean unregister(final int typeId) {
        if (typeId > StorageAdapter.MAX_TYPE_ID || typeId < 0) {
            return false;
        }
        lock.writeLock().lock();
        try {
            final StorageAdapter<?> adapter = idToAdapter.remove((short) typeId);
            if (adapter == null) {
                return false;
            }
            classToAdapter.remove(adapter.type());
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public <T> StorageAdapter<T> findAdapter(final Class<T> type) {
        lock.readLock().lock();
        try {
            StorageAdapter<?> adapter = classToAdapter.get(type);
            if (adapter == null) {
                final Optional<StorageAdapter<?>> adapterOption = classToAdapter.entrySet().stream()
                    .filter(entry -> entry.getKey().isAssignableFrom(type)).findFirst().map(Map.Entry::getValue);
                if (adapterOption.isEmpty()) {
                    return null;
                }
                adapter = adapterOption.get();
            }
            return (StorageAdapter<T>) adapter;
        } finally {
            lock.readLock().unlock();
        }
    }

    public <T> StorageAdapter<T> findAdapter(final int typeId) {
        if (typeId > StorageAdapter.MAX_TYPE_ID || typeId < 0) {
            return null;
        }
        lock.readLock().lock();
        try {
            return (StorageAdapter<T>) idToAdapter.get((short) typeId);
        } finally {
            lock.readLock().unlock();
        }
    }

    public <T> Stored<T> create(final T value) {
        Stored<T> stored = create((Class<T>) value.getClass());
        stored.value(value);
        return stored;
    }

    public <T> Stored<T> create(final Class<T> type) {
        StorageAdapter<T> adapter = findAdapter(type);
        if (adapter == null) {
            throw new IllegalArgumentException("Unknown type: " + type.getName());
        }
        return new Stored<>(this, adapter);
    }

    public <T> Stored<T> create(final int typeId) {
        StorageAdapter<T> adapter = findAdapter(typeId);
        if (adapter == null) {
            throw new IllegalArgumentException("Unknown type id: " + typeId);
        }
        return new Stored<>(this, adapter);
    }

}
