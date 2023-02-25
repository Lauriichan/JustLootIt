package me.lauriichan.spigot.justlootit.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public abstract class Storage<S extends Storable> {

    protected final HashMap<Class<? extends S>, StorageAdapter<? extends S>> adapters = new HashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    protected final Class<S> baseType;
    
    public Storage(final Class<S> baseType) {
        this.baseType = baseType;
    }
    
    public final Class<S> baseType() {
        return baseType;
    }

    public final void register(StorageAdapter<? extends S> adapter) {
        if (adapters.containsKey(adapter.type())) {
            throw new IllegalStateException("There is already an adapter for the type '" + adapter.type().getName() + "'!");
        }
        lock.writeLock().lock();
        try {
            adapters.put(adapter.type(), adapter);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public final boolean unregister(Class<? extends S> type) {
        lock.writeLock().lock();
        try {
            return adapters.remove(type) != null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public final StorageAdapter<? extends S> findAdapterFor(Class<? extends S> type) {
        lock.readLock().lock();
        try {
            StorageAdapter<? extends S> adapter = adapters.get(type);
            if (adapter != null) {
                return adapter;
            }
            Optional<StorageAdapter<? extends S>> adapterOption = adapters.entrySet().stream().filter(entry -> entry.getKey().isAssignableFrom(type))
                .findFirst().map(Map.Entry::getValue);
            if (adapterOption.isEmpty()) {
                return null;
            }
            return adapterOption.get();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public abstract void updateEach(Consumer<S> updater);
    
    public abstract void write(S storable);
    
    public abstract S read(long id);

}
