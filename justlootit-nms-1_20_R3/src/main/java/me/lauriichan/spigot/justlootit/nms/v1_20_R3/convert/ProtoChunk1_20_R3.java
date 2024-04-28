package me.lauriichan.spigot.justlootit.nms.v1_20_R3.convert;

import java.util.Map;

import org.joml.Vector3i;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.shorts.Short2ObjectArrayMap;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoBlock;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoBlockEntity;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoChunk;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ProtoChunk1_20_R3 extends ProtoChunk {

    private final ProtoWorld1_20_R3 world;
    private final net.minecraft.world.level.chunk.ProtoChunk chunk;

    private final int x, z;

    private final Short2ObjectArrayMap<ProtoBlockEntity> blockEntities = new Short2ObjectArrayMap<>();
    private final ObjectArrayList<ProtoEntity> entities = new ObjectArrayList<>();

    private volatile boolean dirty = false;

    public ProtoChunk1_20_R3(ProtoWorld1_20_R3 world, net.minecraft.world.level.chunk.ProtoChunk chunk, int x, int z) {
        this.world = world;
        this.chunk = chunk;
        this.x = x;
        this.z = z;
        for (Map.Entry<BlockPos, CompoundTag> entry : chunk.getBlockEntityNbts().entrySet()) {
            blockEntities.put(posToShort(entry.getKey()),
                new ProtoBlockEntity1_20_R3(entry.getKey(), chunk.getBlockState(entry.getKey()), entry.getValue()));
        }
        for (CompoundTag entityTag : chunk.getEntities()) {
            entities.add(new ProtoEntity1_20_R3(entityTag));
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
    public ProtoWorld1_20_R3 getWorld() {
        return world;
    }

    @Override
    public ProtoBlock getBlock(Vector3i vector) {
        ProtoBlock block = blockEntities.get(posToShort(vector));
        if (block != null) {
            return block;
        }
        BlockPos pos = new BlockPos(vector.x(), vector.y(), vector.z());
        return new ProtoBlock1_20_R3(pos, chunk.getBlockState(pos));
    }

    @Override
    public ProtoBlock getBlock(int x, int y, int z) {
        ProtoBlock block = blockEntities.get(posToShort(x, y, z));
        if (block != null) {
            return block;
        }
        BlockPos pos = new BlockPos(x, y, z);
        return new ProtoBlock1_20_R3(pos, chunk.getBlockState(pos));
    }

    @Override
    public ProtoBlockEntity getBlockEntity(Vector3i vector) {
        return blockEntities.get(posToShort(vector.x(), vector.y(), vector.z()));
    }

    @Override
    public ProtoBlockEntity getBlockEntity(int x, int y, int z) {
        return blockEntities.get(posToShort(x, y, z));
    }

    @Override
    public ProtoBlockEntity asBlockEntity(ProtoBlock protoBlock) {
        if (protoBlock instanceof ProtoBlockEntity blockEntity) {
            return blockEntity;
        }
        return blockEntities.get(posToShort(((ProtoBlock1_20_R3) protoBlock).pos()));
    }

    @Override
    public void updateBlock(ProtoBlock protoBlock) {
        if (protoBlock instanceof ProtoBlock1_20_R3 block) {
            BlockState state = block.state();
            chunk.setBlockState(block.pos(), state, false);
            if (state instanceof EntityBlock entityBlock) {
                short id = posToShort(block.pos());
                if (!blockEntities.containsKey(id)) {
                    CompoundTag tag = new CompoundTag();
                    BlockPos pos = block.pos();
                    tag.putString("id", state.getBlockHolder().unwrapKey().get().location().toString());
                    tag.putInt("x", pos.getX());
                    tag.putInt("y", pos.getY());
                    tag.putInt("z", pos.getZ());
                    tag.putBoolean("keepPacked", false);
                    ProtoBlockEntity1_20_R3 blockEntity = new ProtoBlockEntity1_20_R3(pos, state, tag);
                    blockEntities.put(id, blockEntity);
                }
            }
        } else if (protoBlock instanceof ProtoBlockEntity1_20_R3 blockEntity) {
            BlockState state = blockEntity.state();
            chunk.setBlockState(blockEntity.pos(), state, false);
            if (!(state instanceof EntityBlock entityBlock)) {
                blockEntities.remove(posToShort(blockEntity.pos()));
            } else {
                blockEntity.save();
            }
        }
        dirty = true;
    }

    @Override
    public void updateEntity(ProtoEntity entity) {
        ((ProtoEntity1_20_R3) entity).save();
        dirty = true;
    }

    private short posToShort(Vector3i pos) {
        return posToShort(pos.x(), pos.y(), pos.z());
    }

    private short posToShort(BlockPos pos) {
        return posToShort(pos.getX(), pos.getY(), pos.getZ());
    }

    private short posToShort(int x, int y, int z) {
        short pos = 0;
        pos += (short) (x & 0xf);
        pos += (short) (y & 0xf) << 4;
        pos += (short) (z & 0xf) << 8;
        return pos;
    }

}
