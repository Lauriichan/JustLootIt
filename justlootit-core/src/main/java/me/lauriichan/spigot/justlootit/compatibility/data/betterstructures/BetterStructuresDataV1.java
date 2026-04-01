package me.lauriichan.spigot.justlootit.compatibility.data.betterstructures;

import org.bukkit.NamespacedKey;

import me.lauriichan.spigot.justlootit.compatibility.data.CompatibilityDataExtension;

public record BetterStructuresDataV1(CompatibilityDataExtension<?> extension, NamespacedKey dataId, String fileName) implements IBetterStructuresData {

    @Override
    public int version() {
        return 0;
    }

}
