package me.lauriichan.spigot.justlootit.storage.randomaccessfile;

import java.io.Closeable;
import java.io.File;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

import me.lauriichan.spigot.justlootit.storage.StorageException;
import me.lauriichan.spigot.justlootit.storage.util.counter.Counter;

public interface IRAFFile extends Closeable {
    
    int id();
    
    String hexId();
    
    int version();
    
    IRAFSettings settings();
    
    File file();
    
    default boolean exists() {
        return file().exists();
    }
    
    boolean isOpen();
    
    boolean has(long id) throws StorageException;
    
    IRAFEntry read(long id) throws StorageException;
    
    void write(IRAFEntry entry) throws StorageException;
    
    boolean delete(long id) throws StorageException;
    
    Map.Entry<Counter, CompletableFuture<Void>> forEach(Consumer<IRAFEntry> consumer, Executor executor) throws StorageException;
    
    Map.Entry<Counter, CompletableFuture<Void>> modifyEach(Function<IRAFEntry, IRAFEntry> func, Executor executor) throws StorageException;
    
    void open() throws StorageException;
    
    @Override
    void close() throws StorageException;

}
