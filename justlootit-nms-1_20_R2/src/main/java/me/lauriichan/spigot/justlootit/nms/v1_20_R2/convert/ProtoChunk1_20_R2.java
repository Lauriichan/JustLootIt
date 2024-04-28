package me.lauriichan.spigot.justlootit.nms.v1_20_R2.convert;

import java.util.Map;

import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_20_R2.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_20_R2.block.CraftBlockStates;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoChunk;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public class ProtoChunk1_20_R2 extends ProtoChunk {
    
    static interface IUpdatable {}
    
    static record UpdatedBlockState(CraftBlockEntityState<?> state) implements IUpdatable {}

    private final ProtoWorld1_20_R2 world;
    private final net.minecraft.world.level.chunk.ProtoChunk chunk;
    
    private final int x, z;

    private ObjectList<BlockState> blockEntities;
    private ObjectList<ProtoEntity> entities;
    
    final ObjectArrayList<IUpdatable> updated = new ObjectArrayList<>();

    public ProtoChunk1_20_R2(ProtoWorld1_20_R2 world, net.minecraft.world.level.chunk.ProtoChunk chunk, int x, int z) {
        this.world = world;
        this.chunk = chunk;
        this.x = x;
        this.z = z;
    }
    
    @Override
    public int getX() {
        return x;
    }
    
    @Override
    public int getZ() {
        return z;
    }
    
    @Override
    public ProtoWorld1_20_R2 getWorld() {
        return world;
    }

    @Override
    public ObjectList<BlockState> getBlockEntities() {
        if (blockEntities != null) {
            return blockEntities;
        }
        ObjectArrayList<BlockState> blockEntities = new ObjectArrayList<>();
        for (Map.Entry<BlockPos, CompoundTag> entry : chunk.getBlockEntityNbts().entrySet()) {
            blockEntities.add(CraftBlockStates.getBlockState(entry.getKey(), chunk.getBlockState(entry.getKey()), entry.getValue()));
        }
        return this.blockEntities = ObjectLists.unmodifiable(blockEntities);
    }

    @Override
    public ObjectList<ProtoEntity> getEntities() {
        if (entities != null) {
            return entities;
        }
        ObjectArrayList<ProtoEntity> entities = new ObjectArrayList<>();
        for (CompoundTag entityTag : chunk.getEntities()) {
            entities.add(new ProtoEntity1_20_R2(entityTag));
        }
        return this.entities = ObjectLists.unmodifiable(entities);
    }

    @Override
    public void updateBlockEntity(BlockState state) {
        if (!(state instanceof CraftBlockEntityState<?> entityState) || updated.stream().anyMatch(updatable -> updatable instanceof UpdatedBlockState uState && uState.state() == state)) {
            return;
        }
        updated.add(new UpdatedBlockState(entityState));
    }

    @Override
    public void updateEntity(ProtoEntity entity) {
        ProtoEntity1_20_R2 pEntity = (ProtoEntity1_20_R2) entity;
        if (updated.contains(pEntity)) {
            return;
        }
        updated.add(pEntity);
    }

}
