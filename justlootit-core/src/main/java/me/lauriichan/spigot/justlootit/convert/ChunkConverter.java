package me.lauriichan.spigot.justlootit.convert;

import java.util.Random;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoChunk;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoWorld;
import me.lauriichan.spigot.justlootit.nms.nbt.NbtHelper;

public abstract class ChunkConverter {
    
    protected final VersionHandler versionHandler;
    protected final NbtHelper nbtHelper;
    
    protected final ISimpleLogger logger;
    
    protected final ConversionProperties properties;
    
    public ChunkConverter(final VersionHandler versionHandler, final ConversionProperties properties) {
        this.versionHandler = versionHandler;
        this.nbtHelper = versionHandler.nbtHelper();
        this.logger = versionHandler.logger();
        this.properties = properties;
    }
    
    abstract void convert(ProtoChunk chunk, Random random);
    
    abstract boolean isEnabled();
    
    boolean isEnabledFor(ProtoWorld world) {
        return true;
    }

}
