package me.lauriichan.spigot.justlootit.nms.v1_21_R4.convert;

import org.bukkit.craftbukkit.v1_21_R4.entity.CraftEntityType;
import org.bukkit.craftbukkit.v1_21_R4.persistence.CraftPersistentDataContainer;

import me.lauriichan.spigot.justlootit.nms.convert.ProtoEntity;
import me.lauriichan.spigot.justlootit.nms.nbt.ICompoundTag;
import me.lauriichan.spigot.justlootit.nms.v1_21_R4.nbt.CompoundTag1_21_R4;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;

public class ProtoEntity1_21_R4 extends ProtoEntity {

    private final org.bukkit.entity.EntityType type;
    private final CraftPersistentDataContainer container;

    private final CompoundTag1_21_R4 tag;

    public ProtoEntity1_21_R4(CompoundTag entityTag) {
        this.type = CraftEntityType.minecraftToBukkit(EntityType.by(entityTag).get());
        this.container = new CraftPersistentDataContainer(ConversionAdapter1_21_R4.DATA_TYPE_REGISTRY);
        if (entityTag.get("BukkitValues") instanceof CompoundTag tag) {
            container.putAll(tag);
        }
        this.tag = new CompoundTag1_21_R4(entityTag);
    }

    @Override
    public ICompoundTag getNbt() {
        return tag;
    }

    @Override
    public CraftPersistentDataContainer getContainer() {
        return container;
    }

    @Override
    public org.bukkit.entity.EntityType getType() {
        return type;
    }

    public void save() {
        tag.handle().put("BukkitValues", container.toTagCompound());
    }

}
