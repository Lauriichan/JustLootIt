package me.lauriichan.spigot.justlootit.storage;

import java.util.function.Function;

import me.lauriichan.laylib.logger.ISimpleLogger;

public interface IStorage {
    
    ISimpleLogger logger();
    
    StorageAdapterRegistry registry();
    
    boolean isSupported(long id);
    
    boolean has(long id) throws StorageException;
    
    <T> Stored<T> read(long id) throws StorageException;
    
    void write(Stored<?> stored) throws StorageException;
    
    boolean delete(long id) throws StorageException;
    
    void updateEach(Function<Stored<?>, UpdateInfo<?>> updater);
    
    void clear() throws StorageException;
    
    void close() throws StorageException;

}
