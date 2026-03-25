package me.lauriichan.spigot.justlootit.nms.v26_1.model;

import net.minecraft.network.syncher.SynchedEntityData.DataValue;

public class UnknownEntityData26_1 extends EntityData26_1 {

    private final DataValue<?> value;

    UnknownEntityData26_1(final DataValue<?> value) {
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