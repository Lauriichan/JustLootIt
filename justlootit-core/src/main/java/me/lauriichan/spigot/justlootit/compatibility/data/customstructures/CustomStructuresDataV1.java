package me.lauriichan.spigot.justlootit.compatibility.data.customstructures;

import me.lauriichan.spigot.justlootit.compatibility.data.CompatibilityDataExtension;

public record CustomStructuresDataV1(CompatibilityDataExtension<?> extension, String structureName, long seed) implements ICustomStructuresData {

    @Override
    public int version() {
        return 0;
    }

}
