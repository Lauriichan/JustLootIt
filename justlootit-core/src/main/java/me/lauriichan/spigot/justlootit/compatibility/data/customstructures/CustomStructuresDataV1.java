package me.lauriichan.spigot.justlootit.compatibility.data.customstructures;

import org.bukkit.NamespacedKey;

import me.lauriichan.spigot.justlootit.compatibility.data.CompatibilityDataExtension;

public record CustomStructuresDataV1(CompatibilityDataExtension<?> extension, NamespacedKey dataId, String structureName, long seed) implements ICustomStructuresData {

    @Override
    public int version() {
        return 0;
    }

}
