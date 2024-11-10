package me.lauriichan.spigot.justlootit.storage;

import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.storage.util.counter.CounterProgress;

public interface IStorage {
    
    ISimpleLogger logger();
    
    StorageAdapterRegistry registry();
    
    boolean isSupported(long id);
    
    boolean has(long id) throws StorageException;
    
    <T> Stored<T> read(long id) throws StorageException;
    
    void write(Stored<?> stored) throws StorageException;
    
    boolean delete(long id) throws StorageException;
    
    CounterProgress forEach(Consumer<Stored<?>> reader, Executor executor);
    
    CounterProgress updateEach(Function<Stored<?>, UpdateInfo<?>> updater, Executor executor);
    
    void clear() throws StorageException;
    
    void close() throws StorageException;

}
