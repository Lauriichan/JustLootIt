package me.lauriichan.spigot.justlootit.storage;

import java.util.function.Function;

import me.lauriichan.laylib.logger.ISimpleLogger;

public interface IStorage<S extends Storable> {

    ISimpleLogger logger();

    Class<S> baseType();

    void register(StorageAdapter<? extends S> adapter) throws StorageException;

    boolean unregister(Class<? extends S> type);

    StorageAdapter<? extends S> findAdapterFor(Class<? extends S> type);

    StorageAdapter<? extends S> findAdapterFor(short typeId);

    boolean isSupported(long id);

    void close() throws StorageException;

    void clear() throws StorageException;

    boolean has(long id) throws StorageException;

    S read(long id) throws StorageException;

    void write(S storable) throws StorageException;

    boolean delete(long id) throws StorageException;

    void updateEach(Function<S, UpdateInfo<S>> updater);

}
