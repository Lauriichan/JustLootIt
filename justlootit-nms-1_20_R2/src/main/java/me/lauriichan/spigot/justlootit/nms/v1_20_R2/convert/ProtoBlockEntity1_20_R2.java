package me.lauriichan.spigot.justlootit.nms.v1_20_R2.convert;

import java.util.Objects;

import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R2.persistence.CraftPersistentDataContainer;
import org.bukkit.persistence.PersistentDataContainer;
import org.joml.Vector3i;

import me.lauriichan.spigot.justlootit.nms.convert.ProtoBlockEntity;
import me.lauriichan.spigot.justlootit.nms.nbt.ICompoundTag;
import me.lauriichan.spigot.justlootit.nms.v1_20_R2.nbt.CompoundTag1_20_R2;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class ProtoBlockEntity1_20_R2 extends ProtoBlockEntity {

    private final BlockPos pos;
    
    private final CompoundTag1_20_R2 tag;
    private final CraftPersistentDataContainer container;
    
    private volatile CraftBlockData data;
    
    public ProtoBlockEntity1_20_R2(BlockPos pos, BlockState state, CompoundTag blockTag) {
        this.pos = pos;
        this.data = CraftBlockData.fromData(state);
        this.container = new CraftPersistentDataContainer(ConversionAdapter1_20_R2.DATA_TYPE_REGISTRY);
        if (blockTag.get("PublicBukkitValues") instanceof CompoundTag tag) {
            container.putAll(tag);
        }
        this.tag = new CompoundTag1_20_R2(blockTag);
    }

    @Override
    public BlockData getData() {
        return data;
    }

    @Override
    public void setData(BlockData blockData) {
        this.data = (CraftBlockData) Objects.requireNonNull(blockData);
    }

    @Override
    public Vector3i getPos() {
        return new Vector3i(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public PersistentDataContainer getContainer() {
        return container;
    }

    @Override
    public ICompoundTag getNbt() {
        return tag;
    }
    
    public BlockPos pos() {
        return pos;
    }
    
    public BlockState state() {
        return data.getState();
    }
    
    public void save() {
        tag.handle().put("PublicBukkitValues", container.toTagCompound());
    }

    public CompoundTag tag() {
        return tag.handle();
    }

}
