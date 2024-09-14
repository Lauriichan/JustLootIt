package me.lauriichan.spigot.justlootit.nms.v1_20_R2.convert;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.shorts.Short2ObjectArrayMap;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoBlock;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoBlockEntity;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoChunk;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoEntity;
import me.lauriichan.spigot.justlootit.nms.util.Vec3i;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ProtoChunk1_20_R2 extends ProtoChunk {

    private final ProtoWorld1_20_R2 world;
    private final net.minecraft.world.level.chunk.ProtoChunk chunk;
    
    private final int x, z;

    private final Short2ObjectArrayMap<ProtoBlockEntity> blockEntities = new Short2ObjectArrayMap<>();
    private final ObjectArrayList<ProtoEntity> entities = new ObjectArrayList<>();

    private volatile boolean dirty = false;

    public ProtoChunk1_20_R2(ProtoWorld1_20_R2 world, net.minecraft.world.level.chunk.ProtoChunk chunk, int x, int z) {
        this.world = world;
        this.chunk = chunk;
        this.x = x;
        this.z = z;
        for (Map.Entry<BlockPos, CompoundTag> entry : chunk.getBlockEntityNbts().entrySet()) {
            blockEntities.put(packShort(entry.getKey()),
                new ProtoBlockEntity1_20_R2(entry.getKey(), chunk.getBlockState(entry.getKey()), entry.getValue()));
        }
        for (CompoundTag entityTag : chunk.getEntities()) {
            entities.add(new ProtoEntity1_20_R2(entityTag));
        }
    }
    
    public boolean isDirty() {
        return dirty;
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
    public ObjectCollection<ProtoBlockEntity> getBlockEntities() {
        return blockEntities.values();
    }

    @Override
    public ObjectList<ProtoEntity> getEntities() {
        return ObjectLists.unmodifiable(entities);
    }

    @Override
    public ProtoWorld1_20_R2 getWorld() {
        return world;
    }

    @Override
    public ProtoBlock getBlock(Vec3i vector) {
        ProtoBlock block = blockEntities.get(vector.packShort());
        if (block != null) {
            return block;
        }
        BlockPos pos = new BlockPos(vector.x(), vector.y(), vector.z());
        return new ProtoBlock1_20_R2(pos, chunk.getBlockState(pos));
    }

    @Override
    public ProtoBlock getBlock(int x, int y, int z) {
        ProtoBlock block = blockEntities.get(Vec3i.packShort(x, y, z));
        if (block != null) {
            return block;
        }
        BlockPos pos = new BlockPos(x, y, z);
        return new ProtoBlock1_20_R2(pos, chunk.getBlockState(pos));
    }

    @Override
    public ProtoBlockEntity getBlockEntity(Vec3i vector) {
        return blockEntities.get(vector.packShort());
    }

    @Override
    public ProtoBlockEntity getBlockEntity(int x, int y, int z) {
        return blockEntities.get(Vec3i.packShort(x, y, z));
    }

    @Override
    public ProtoBlockEntity asBlockEntity(ProtoBlock protoBlock) {
        if (protoBlock instanceof ProtoBlockEntity blockEntity) {
            return blockEntity;
        }
        return blockEntities.get(packShort(((ProtoBlock1_20_R2) protoBlock).pos()));
    }

    @Override
    public void updateBlock(ProtoBlock protoBlock) {
        if (protoBlock instanceof ProtoBlock1_20_R2 block) {
            BlockState state = block.state();
            chunk.setBlockState(block.pos(), state, false);
            if (state.getBlock() instanceof EntityBlock) {
                short id = packShort(block.pos());
                if (!blockEntities.containsKey(id)) {
                    CompoundTag tag = new CompoundTag();
                    BlockPos pos = block.pos();
                    tag.putString("id", state.getBlockHolder().unwrapKey().get().location().toString());
                    tag.putInt("x", pos.getX());
                    tag.putInt("y", pos.getY());
                    tag.putInt("z", pos.getZ());
                    tag.putBoolean("keepPacked", false);
                    ProtoBlockEntity1_20_R2 blockEntity = new ProtoBlockEntity1_20_R2(pos, state, tag);
                    blockEntities.put(id, blockEntity);
                }
            }
        } else if (protoBlock instanceof ProtoBlockEntity1_20_R2 blockEntity) {
            BlockState state = blockEntity.state();
            chunk.setBlockState(blockEntity.pos(), state, false);
            if (!(state.getBlock() instanceof EntityBlock)) {
                blockEntities.remove(packShort(blockEntity.pos()));
            } else {
                blockEntity.tag().putString("id", state.getBlockHolder().unwrapKey().get().location().toString());
                blockEntity.save();
                blockEntity.updateEntity();
            }
        }
        dirty = true;
    }

    @Override
    public void updateEntity(ProtoEntity entity) {
        ((ProtoEntity1_20_R2) entity).save();
        dirty = true;
    }

    private short packShort(BlockPos pos) {
        return Vec3i.packShort(pos.getX(), pos.getY(), pos.getZ());
    }

}
