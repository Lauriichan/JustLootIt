package me.lauriichan.spigot.justlootit.storage.randomaccessfile;

import java.io.File;
import java.io.IOException;

public interface IRAFFile {
    
    int id();
    
    String hexId();
    
    int version();
    
    File file();
    
    boolean isOpen();
    
    boolean has(long id);
    
    IRAFEntry read(long id);
    
    void write(IRAFEntry entry);
    
    boolean delete(long id);
    
    void open() throws IOException;
    
    void close() throws IOException;

}
