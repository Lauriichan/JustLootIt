package me.lauriichan.spigot.justlootit.nms.v1_20_R3.convert;

import java.util.List;
import java.util.Objects;

import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_20_R3.persistence.CraftPersistentDataContainer;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;

import me.lauriichan.spigot.justlootit.nms.convert.ProtoBlockEntity;
import me.lauriichan.spigot.justlootit.nms.nbt.ICompoundTag;
import me.lauriichan.spigot.justlootit.nms.util.Vec3i;
import me.lauriichan.spigot.justlootit.nms.v1_20_R3.nbt.CompoundTag1_20_R3;
import me.lauriichan.spigot.justlootit.nms.v1_20_R3.util.NonNullItemList1_20_R3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ProtoBlockEntity1_20_R3 extends ProtoBlockEntity {

    private final BlockPos pos;
    
    private final CompoundTag1_20_R3 tag;
    private final CraftPersistentDataContainer container;
    
    private volatile CraftBlockData data;
    private volatile BlockEntity entity;
    
    private volatile CraftInventory inventory;
    
    public ProtoBlockEntity1_20_R3(BlockPos pos, BlockState state, CompoundTag blockTag) {
        this.pos = pos;
        this.data = CraftBlockData.fromData(state);
        this.container = new CraftPersistentDataContainer(ConversionAdapter1_20_R3.DATA_TYPE_REGISTRY);
        if (blockTag.get("PublicBukkitValues") instanceof CompoundTag tag) {
            container.putAll(tag);
        }
        this.tag = new CompoundTag1_20_R3(blockTag);
        updateEntity();
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
    public Vec3i getPos() {
        return new Vec3i(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public PersistentDataContainer getContainer() {
        return container;
    }

    @Override
    public ICompoundTag getNbt() {
        return tag;
    }
    
    @Override
    public boolean hasTileEntity() {
        return entity != null;
    }
    
    @Override
    public boolean hasInventory() {
        return inventory != null;
    }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
    
    public BlockPos pos() {
        return pos;
    }
    
    public BlockState state() {
        return data.getState();
    }
    
    public void save() {
        tag.handle().put("PublicBukkitValues", container.toTagCompound());
        if (inventory != null) {
            List<ItemStack> list = inventory.getInventory().getContents();
            NonNullList<ItemStack> saveList;
            if (list instanceof NonNullList<ItemStack> nonNull) {
                saveList = nonNull;
            } else {
                saveList = NonNullItemList1_20_R3.of(list);
            }
            ContainerHelper.saveAllItems(tag.handle(), saveList);
        }
    }
    
    public void updateEntity() {
        if (this.entity != null && BlockEntityType.getKey(entity.getType()).toString().equals(tag.getString("id"))) {
            // No need to update
            return;
        }
        this.entity = null;
        this.inventory = null;
        String blockEntityId = tag.getString("id");
        if (blockEntityId != null) {
            BlockEntityType<?> type = BuiltInRegistries.BLOCK_ENTITY_TYPE.get(ResourceLocation.tryParse(blockEntityId));
            if (type != null) {
                this.entity = type.create(pos, data.getState());
                if (entity instanceof Container container) {
                    this.inventory = new CraftInventory(container);
                }
            }
        }
    }

    public CompoundTag tag() {
        return tag.handle();
    }

}
