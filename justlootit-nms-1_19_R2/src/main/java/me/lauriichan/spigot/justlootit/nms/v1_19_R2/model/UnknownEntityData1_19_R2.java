package me.lauriichan.spigot.justlootit.nms.v1_19_R2.model;

import net.minecraft.network.syncher.SynchedEntityData.DataValue;

public class UnknownEntityData1_19_R2 extends EntityData1_19_R2 {

    private final DataValue<?> value;

    UnknownEntityData1_19_R2(DataValue<?> value) {
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
