package me.lauriichan.spigot.justlootit.nms.v26_2.model;

import net.minecraft.network.syncher.SynchedEntityData.DataValue;

public class UnknownEntityData26_2 extends EntityData26_2 {

    private final DataValue<?> value;

    UnknownEntityData26_2(final DataValue<?> value) {
        this.value = value;
    }

    @Override
    public int getId() {
        return value.id();
    }

    @Override
    public DataValue<?> build() {
        return value;
    }

}