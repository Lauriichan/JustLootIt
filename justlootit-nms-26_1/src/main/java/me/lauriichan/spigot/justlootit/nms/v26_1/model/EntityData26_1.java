package me.lauriichan.spigot.justlootit.nms.v26_1.model;

import me.lauriichan.spigot.justlootit.nms.model.IEntityData;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.world.item.ItemStack;

public abstract class EntityData26_1 implements IEntityData {

    @SuppressWarnings("unchecked")
    public static EntityData26_1 create(final DataValue<?> value) {
        final Object object = value.value();
        if (object instanceof ItemStack) {
            return new ItemEntityData26_1((DataValue<ItemStack>) value);
        }
        return new UnknownEntityData26_1(value);
    }

    public abstract DataValue<?> build();

}