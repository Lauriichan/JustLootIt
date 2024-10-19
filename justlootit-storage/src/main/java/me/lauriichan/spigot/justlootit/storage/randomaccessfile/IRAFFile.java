package me.lauriichan.spigot.justlootit.storage.randomaccessfile;

import java.io.Closeable;
import java.io.File;
import java.util.function.Consumer;
import java.util.function.Function;

import me.lauriichan.spigot.justlootit.storage.StorageException;

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
    
    void forEach(Consumer<IRAFEntry> consumer) throws StorageException;
    
    void modifyEach(Function<IRAFEntry, IRAFEntry> func) throws StorageException;
    
    void open() throws StorageException;
    
    @Override
    void close() throws StorageException;

}
