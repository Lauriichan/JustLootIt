package me.lauriichan.spigot.justlootit.nms.convert;

import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.spigot.justlootit.nms.util.Vec3i;

public abstract class ProtoChunk {
    
    public abstract int getX();
    
    public abstract int getZ();
    
    public long getPosAsLong() {
        return (long) getX() & 4294967295L | ((long) getZ() & 4294967295L) << 32;
    }
    
    public abstract ObjectCollection<ProtoBlockEntity> getBlockEntities();
    
    public abstract ObjectList<ProtoEntity> getEntities();
    
    public abstract ProtoWorld getWorld();
    
    public abstract ProtoBlock getBlock(Vec3i vector);
    
    public abstract ProtoBlock getBlock(int x, int y, int z);
    
    public abstract ProtoBlockEntity getBlockEntity(Vec3i vector);
    
    public abstract ProtoBlockEntity getBlockEntity(int x, int y, int z);
    
    public abstract ProtoBlockEntity asBlockEntity(ProtoBlock block);
    
    public abstract void updateBlock(ProtoBlock block);
    
    public abstract void updateEntity(ProtoEntity entity);
    
}
