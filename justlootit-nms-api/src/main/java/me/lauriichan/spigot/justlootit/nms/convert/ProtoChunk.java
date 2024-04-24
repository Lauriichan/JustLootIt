package me.lauriichan.spigot.justlootit.nms.convert;

import org.bukkit.block.BlockState;

import it.unimi.dsi.fastutil.objects.ObjectList;

public abstract class ProtoChunk {
    
    public abstract ObjectList<BlockState> getBlockEntities();
    
    public abstract ObjectList<ProtoEntity> getEntities();
    
    public abstract void updateBlockEntity(BlockState state);
    
    public abstract void updateEntity(ProtoEntity entity);
    
}
