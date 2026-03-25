package me.lauriichan.spigot.justlootit.nms.v26_1.convert;

import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;

import me.lauriichan.spigot.justlootit.nms.convert.ProtoEntity;
import me.lauriichan.spigot.justlootit.nms.nbt.ICompoundTag;
import me.lauriichan.spigot.justlootit.nms.v26_1.nbt.CompoundTag26_1;
import net.minecraft.core.RegistryAccess.Frozen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.TagValueInput;

public class ProtoEntity26_1 extends ProtoEntity {

    private final org.bukkit.entity.EntityType type;
    private final CraftPersistentDataContainer container;

    private final CompoundTag26_1 tag;

    public ProtoEntity26_1(Frozen registry, CompoundTag entityTag) {
        this.type = CraftEntityType
            .minecraftToBukkit(EntityType.by(TagValueInput.create(ProblemReporter.DISCARDING, registry, entityTag)).get());
        this.container = new CraftPersistentDataContainer(ConversionAdapter26_1.DATA_TYPE_REGISTRY);
        if (entityTag.get("BukkitValues") instanceof CompoundTag tag) {
            container.putAll(tag);
        }
        this.tag = new CompoundTag26_1(entityTag);
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
