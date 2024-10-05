package me.lauriichan.spigot.justlootit.storage.randomaccessfile;

import io.netty.buffer.ByteBuf;

public interface IRAFEntry {
    
    long id();
    
    int typeId();
    
    int version();
    
    ByteBuf buffer();

}
