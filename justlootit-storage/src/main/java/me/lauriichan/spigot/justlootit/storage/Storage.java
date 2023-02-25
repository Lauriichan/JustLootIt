package me.lauriichan.spigot.justlootit.storage;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public abstract class Storage<S extends Storable> {

    protected final Object2ObjectOpenHashMap<Class<? extends S>, StorageAdapter<? extends S>> classToAdapter = new Object2ObjectOpenHashMap<>();
    protected final Short2ObjectOpenHashMap<StorageAdapter<? extends S>> idToAdapter = new Short2ObjectOpenHashMap<>();

    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    protected final Class<S> baseType;

    public Storage(final Class<S> baseType) {
        this.baseType = baseType;
    }

    public final Class<S> baseType() {
        return baseType;
    }

    public final void register(StorageAdapter<? extends S> adapter) throws StorageException {
        if (classToAdapter.containsKey(adapter.type())) {
            throw new StorageException("There is already an adapter for the type '" + adapter.type().getName() + "'!");
        }
        if (idToAdapter.containsKey(adapter.typeId())) {
            throw new StorageException("There is already an adapter with the type id " + adapter.typeId() + "!");
        }
        lock.writeLock().lock();
        try {
            classToAdapter.put(adapter.type(), adapter);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public final boolean unregister(Class<? extends S> type) {
        lock.writeLock().lock();
        try {
            StorageAdapter<?> adapter = classToAdapter.remove(type);
            if(adapter == null) {
                return false;
            }
            idToAdapter.remove(adapter.typeId());
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public final boolean unregister(short typeId) {
        lock.writeLock().lock();
        try {
            StorageAdapter<?> adapter = idToAdapter.remove(typeId);
            if(adapter == null) {
                return false;
            }
            classToAdapter.remove(adapter.type());
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public final StorageAdapter<? extends S> findAdapterFor(Class<? extends S> type) {
        lock.readLock().lock();
        try {
            StorageAdapter<? extends S> adapter = classToAdapter.get(type);
            if (adapter != null) {
                return adapter;
            }
            Optional<StorageAdapter<? extends S>> adapterOption = classToAdapter.entrySet().stream()
                .filter(entry -> entry.getKey().isAssignableFrom(type)).findFirst().map(Map.Entry::getValue);
            if (adapterOption.isEmpty()) {
                return null;
            }
            return adapterOption.get();
        } finally {
            lock.readLock().unlock();
        }
    }

    public final StorageAdapter<? extends S> findAdapterFor(short typeId) {
        lock.readLock().lock();
        try {
            return idToAdapter.get(typeId);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public abstract void close() throws StorageException;
    
    public abstract void clear() throws StorageException;

    public abstract S read(long id) throws StorageException;

    public abstract void write(S storable) throws StorageException;

    public abstract boolean delete(long id) throws StorageException;

    public abstract void updateEach(Consumer<S> updater) throws StorageException;

}
