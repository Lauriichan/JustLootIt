package me.lauriichan.spigot.justlootit.storage.randomaccessfile;

import java.io.Closeable;
import java.io.File;
import java.util.function.Consumer;
import java.util.function.Function;

import io.netty.buffer.ByteBuf;
import me.lauriichan.spigot.justlootit.storage.StorageException;
import me.lauriichan.spigot.justlootit.storage.util.Tuple;
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
    
    IRAFEntry newEntry(long id, int typeId, int version, ByteBuf buffer);
    
    boolean isOpen();
    
    boolean has(long id) throws StorageException;
    
    IRAFEntry read(long id) throws StorageException;
    
    void write(IRAFEntry entry) throws StorageException;
    
    boolean delete(long id) throws StorageException;
    
    Tuple<Counter, Runnable> forEach(Consumer<IRAFEntry> consumer);
    
    Tuple<Counter, Runnable> modifyEach(Function<IRAFEntry, IRAFEntry> func);
    
    void open() throws StorageException;
    
    @Override
    void close() throws StorageException;

}
