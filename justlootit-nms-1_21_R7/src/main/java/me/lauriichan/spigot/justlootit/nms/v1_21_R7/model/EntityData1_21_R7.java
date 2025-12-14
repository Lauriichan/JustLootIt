package me.lauriichan.spigot.justlootit.nms.v1_21_R7.model;

import me.lauriichan.spigot.justlootit.nms.model.IEntityData;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.world.item.ItemStack;

public abstract class EntityData1_21_R7 implements IEntityData {

    @SuppressWarnings("unchecked")
    public static EntityData1_21_R7 create(final DataValue<?> value) {
        final Object object = value.value();
        if (object instanceof ItemStack) {
            return new ItemEntityData1_21_R7((DataValue<ItemStack>) value);
        }
        return new UnknownEntityData1_21_R7(value);
    }

    public abstract DataValue<?> build();

}