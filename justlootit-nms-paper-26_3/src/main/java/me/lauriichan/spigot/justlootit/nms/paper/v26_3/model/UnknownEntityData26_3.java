package me.lauriichan.spigot.justlootit.nms.paper.v26_3.model;

import net.minecraft.network.syncher.SynchedEntityData.DataValue;

public class UnknownEntityData26_3 extends EntityData26_3 {

    private final DataValue<?> value;

    UnknownEntityData26_3(final DataValue<?> value) {
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