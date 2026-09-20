package me.lauriichan.spigot.justlootit.nms.v26_3.model;

import me.lauriichan.spigot.justlootit.nms.model.IEntityData;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.world.item.ItemStack;

public abstract class EntityData26_3 implements IEntityData {

    @SuppressWarnings("unchecked")
    public static EntityData26_3 create(final DataValue<?> value) {
        final Object object = value.value();
        if (object instanceof ItemStack) {
            return new ItemEntityData26_3((DataValue<ItemStack>) value);
        }
        return new UnknownEntityData26_3(value);
    }

    public abstract DataValue<?> build();

}