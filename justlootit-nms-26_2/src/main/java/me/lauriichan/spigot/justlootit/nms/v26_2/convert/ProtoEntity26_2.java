package me.lauriichan.spigot.justlootit.nms.v26_2.convert;

import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;

import me.lauriichan.spigot.justlootit.nms.convert.ProtoEntity;
import me.lauriichan.spigot.justlootit.nms.nbt.ICompoundTag;
import me.lauriichan.spigot.justlootit.nms.v26_2.nbt.CompoundTag26_2;
import net.minecraft.core.RegistryAccess.Frozen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.TagValueInput;

public class ProtoEntity26_2 extends ProtoEntity {

    private final org.bukkit.entity.EntityType type;
    private final CraftPersistentDataContainer container;

    private final CompoundTag26_2 tag;

    public ProtoEntity26_2(Frozen registry, CompoundTag entityTag) {
        this.type = CraftEntityType
            .minecraftToBukkit(EntityType.by(TagValueInput.create(ProblemReporter.DISCARDING, registry, entityTag)).get());
        this.container = new CraftPersistentDataContainer(ConversionAdapter26_2.DATA_TYPE_REGISTRY);
        if (entityTag.get("BukkitValues") instanceof CompoundTag tag) {
            container.putAll(tag);
        }
        this.tag = new CompoundTag26_2(entityTag);
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
