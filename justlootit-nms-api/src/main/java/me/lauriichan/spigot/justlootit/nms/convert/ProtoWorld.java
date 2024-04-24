package me.lauriichan.spigot.justlootit.nms.convert;

import java.util.function.Consumer;

public abstract class ProtoWorld {
    
    public abstract ConversionProgress streamChunks(Consumer<ProtoChunk> consumer);

}
