package me.lauriichan.spigot.justlootit.nms.v1_20_R4.model;

import me.lauriichan.spigot.justlootit.nms.model.IEntityData;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.world.item.ItemStack;

public abstract class EntityData1_20_R4 implements IEntityData {

    @SuppressWarnings("unchecked")
    public static EntityData1_20_R4 create(final DataValue<?> value) {
        final Object object = value.value();
        if (object instanceof ItemStack) {
            return new ItemEntityData1_20_R4((DataValue<ItemStack>) value);
        }
        return new UnknownEntityData1_20_R4(value);
    }

    public abstract DataValue<?> build();

}