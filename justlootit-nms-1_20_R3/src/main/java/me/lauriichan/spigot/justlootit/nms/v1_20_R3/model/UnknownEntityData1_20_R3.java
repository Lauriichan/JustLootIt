package me.lauriichan.spigot.justlootit.nms.v1_20_R3.model;

import net.minecraft.network.syncher.SynchedEntityData.DataValue;

public class UnknownEntityData1_20_R3 extends EntityData1_20_R3 {

    private final DataValue<?> value;

    UnknownEntityData1_20_R3(final DataValue<?> value) {
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