package me.lauriichan.spigot.justlootit.convert;

import me.lauriichan.spigot.justlootit.config.ConversionConfig;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoChunk;

public abstract class ChunkConverter {
    
    protected final ConversionConfig config;
    
    public ChunkConverter(final ConversionConfig config) {
        this.config = config;
    }
    
    abstract void convert(ProtoChunk chunk);
    
    abstract boolean isEnabled();

}
