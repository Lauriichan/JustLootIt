package me.lauriichan.spigot.justlootit.convert;

import me.lauriichan.spigot.justlootit.nms.convert.ProtoChunk;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoWorld;

public abstract class ChunkConverter {
    
    protected final ConversionProperties properties;
    
    public ChunkConverter(final ConversionProperties properties) {
        this.properties = properties;
    }
    
    abstract void convert(ProtoChunk chunk);
    
    abstract boolean isEnabled();
    
    boolean isEnabledFor(ProtoWorld world) {
        return true;
    }

}
