package me.lauriichan.spigot.justlootit.nms.v1_20_R2.convert;

import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntityType;
import org.bukkit.craftbukkit.v1_20_R2.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_20_R2.persistence.CraftPersistentDataTypeRegistry;

import me.lauriichan.laylib.reflection.JavaAccess;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoEntity;
import me.lauriichan.spigot.justlootit.nms.nbt.ICompoundTag;
import me.lauriichan.spigot.justlootit.nms.v1_20_R2.convert.ProtoChunk1_20_R2.IUpdatable;
import me.lauriichan.spigot.justlootit.nms.v1_20_R2.nbt.CompoundTag1_20_R2;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;

public class ProtoEntity1_20_R2 extends ProtoEntity implements IUpdatable {

    private static final CraftPersistentDataTypeRegistry registry;

    static {
        registry = (CraftPersistentDataTypeRegistry) JavaAccess.getStaticValue(CraftEntity.class, "DATA_TYPE_REGISTRY");
    }

    private final org.bukkit.entity.EntityType type;
    private final CraftPersistentDataContainer container;
    
    private final CompoundTag1_20_R2 tag;

    public ProtoEntity1_20_R2(CompoundTag entityTag) {
        this.type = CraftEntityType.minecraftToBukkit(EntityType.byString(entityTag.getString("id")).get());
        this.container = new CraftPersistentDataContainer(registry);
        if (entityTag.get("BukkitValues") instanceof CompoundTag tag) {
            container.putAll(tag);
        }
        this.tag = new CompoundTag1_20_R2(entityTag);
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
