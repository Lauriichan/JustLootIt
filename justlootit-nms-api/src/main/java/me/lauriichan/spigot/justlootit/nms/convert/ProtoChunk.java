package me.lauriichan.spigot.justlootit.nms.convert;

import org.joml.Vector3i;

import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectList;

public abstract class ProtoChunk {
    
    public abstract int getX();
    
    public abstract int getZ();
    
    public long getPosAsLong() {
        return (long) getX() & 4294967295L | ((long) getZ() & 4294967295L) << 32;
    }
    
    public abstract ObjectCollection<ProtoBlockEntity> getBlockEntities();
    
    public abstract ObjectList<ProtoEntity> getEntities();
    
    public abstract ProtoWorld getWorld();
    
    public abstract ProtoBlock getBlock(Vector3i vector);
    
    public abstract ProtoBlock getBlock(int x, int y, int z);
    
    public abstract ProtoBlockEntity getBlockEntity(Vector3i vector);
    
    public abstract ProtoBlockEntity getBlockEntity(int x, int y, int z);
    
    public abstract ProtoBlockEntity asBlockEntity(ProtoBlock block);
    
    public abstract void updateBlock(ProtoBlock block);
    
    public abstract void updateEntity(ProtoEntity entity);
    
}
