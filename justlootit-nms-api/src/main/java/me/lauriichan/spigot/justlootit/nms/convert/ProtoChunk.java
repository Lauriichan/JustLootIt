package me.lauriichan.spigot.justlootit.nms.convert;

import org.bukkit.block.BlockState;

import it.unimi.dsi.fastutil.objects.ObjectList;

public abstract class ProtoChunk {
    
    public abstract int getX();
    
    public abstract int getZ();
    
    public long getPosAsLong() {
        return (long) getX() & 4294967295L | ((long) getZ() & 4294967295L) << 32;
    }
    
    public abstract ProtoWorld getWorld();
    
    public abstract ObjectList<BlockState> getBlockEntities();
    
    public abstract ObjectList<ProtoEntity> getEntities();
    
    public abstract void updateBlockEntity(BlockState state);
    
    public abstract void updateEntity(ProtoEntity entity);
    
}
