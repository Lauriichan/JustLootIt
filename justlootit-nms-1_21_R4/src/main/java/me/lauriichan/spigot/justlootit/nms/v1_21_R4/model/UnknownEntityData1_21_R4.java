package me.lauriichan.spigot.justlootit.nms.v1_21_R4.model;

import net.minecraft.network.syncher.SynchedEntityData.DataValue;

public class UnknownEntityData1_21_R4 extends EntityData1_21_R4 {

    private final DataValue<?> value;

    UnknownEntityData1_21_R4(final DataValue<?> value) {
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