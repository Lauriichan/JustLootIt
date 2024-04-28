package me.lauriichan.spigot.justlootit.nms.convert;

import java.io.File;
import java.util.function.Consumer;

import me.lauriichan.spigot.justlootit.nms.capability.Capable;

public abstract class ProtoWorld extends Capable<ProtoWorld> implements AutoCloseable {
    
    public abstract long getSeed();
    
    public abstract String getName();
    
    public abstract File getWorldFolder();
    
    public abstract ConversionProgress streamChunks(Consumer<ProtoChunk> consumer);
    
    @Override
    public void close() {
        terminate();
    }

}
