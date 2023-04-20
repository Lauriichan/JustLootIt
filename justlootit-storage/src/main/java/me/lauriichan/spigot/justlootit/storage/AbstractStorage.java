package me.lauriichan.spigot.justlootit.storage;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import me.lauriichan.laylib.logger.ISimpleLogger;

public abstract class AbstractStorage<S extends Storable> implements IStorage<S> {

    protected final Object2ObjectOpenHashMap<Class<? extends S>, StorageAdapter<? extends S>> classToAdapter = new Object2ObjectOpenHashMap<>();
    protected final Short2ObjectOpenHashMap<StorageAdapter<? extends S>> idToAdapter = new Short2ObjectOpenHashMap<>();

    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    protected final ISimpleLogger logger;
    protected final Class<S> baseType;

    public AbstractStorage(final ISimpleLogger logger, final Class<S> baseType) {
        this.logger = logger;
        this.baseType = baseType;
    }

    @Override
    public final ISimpleLogger logger() {
        return logger;
    }

    @Override
    public final Class<S> baseType() {
        return baseType;
    }

    @Override
    public final void register(final StorageAdapter<? extends S> adapter) throws StorageException {
        if (classToAdapter.containsKey(adapter.type())) {
            throw new StorageException("There is already an adapter for the type '" + adapter.type().getName() + "'!");
        }
        if (idToAdapter.containsKey(adapter.typeId())) {
            throw new StorageException("There is already an adapter with the type id " + adapter.typeId() + "!");
        }
        lock.writeLock().lock();
        try {
            idToAdapter.put(adapter.typeId(), adapter);
            classToAdapter.put(adapter.type(), adapter);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public final boolean unregister(final Class<? extends S> type) {
        lock.writeLock().lock();
        try {
            final StorageAdapter<?> adapter = classToAdapter.remove(type);
            if (adapter == null) {
                return false;
            }
            idToAdapter.remove(adapter.typeId());
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public final boolean unregister(final short typeId) {
        lock.writeLock().lock();
        try {
            final StorageAdapter<?> adapter = idToAdapter.remove(typeId);
            if (adapter == null) {
                return false;
            }
            classToAdapter.remove(adapter.type());
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public final StorageAdapter<? extends S> findAdapterFor(final Class<? extends S> type) {
        lock.readLock().lock();
        try {
            final StorageAdapter<? extends S> adapter = classToAdapter.get(type);
            if (adapter != null) {
                return adapter;
            }
            final Optional<StorageAdapter<? extends S>> adapterOption = classToAdapter.entrySet().stream()
                .filter(entry -> entry.getKey().isAssignableFrom(type)).findFirst().map(Map.Entry::getValue);
            if (adapterOption.isEmpty()) {
                return null;
            }
            return adapterOption.get();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public final StorageAdapter<? extends S> findAdapterFor(final short typeId) {
        lock.readLock().lock();
        try {
            return idToAdapter.get(typeId);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public abstract boolean isSupported(long id);

    @Override
    public abstract void close() throws StorageException;

    @Override
    public abstract void clear() throws StorageException;

    @Override
    public abstract boolean has(long id) throws StorageException;

    @Override
    public abstract S read(long id) throws StorageException;

    @Override
    public abstract void write(S storable) throws StorageException;

    @Override
    public abstract boolean delete(long id) throws StorageException;

    @Override
    public abstract void updateEach(Function<S, UpdateInfo<S>> updater) throws StorageException;

}
