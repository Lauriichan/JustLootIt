package me.lauriichan.spigot.justlootit.nms.paper.v26_2.model;

import me.lauriichan.spigot.justlootit.nms.model.IEntityData;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.world.item.ItemStack;

public abstract class EntityData26_2 implements IEntityData {

    @SuppressWarnings("unchecked")
    public static EntityData26_2 create(final DataValue<?> value) {
        final Object object = value.value();
        if (object instanceof ItemStack) {
            return new ItemEntityData26_2((DataValue<ItemStack>) value);
        }
        return new UnknownEntityData26_2(value);
    }

    public abstract DataValue<?> build();

}