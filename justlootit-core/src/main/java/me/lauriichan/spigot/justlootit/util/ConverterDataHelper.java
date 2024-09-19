package me.lauriichan.spigot.justlootit.util;

import org.bukkit.persistence.PersistentDataContainer;

import me.lauriichan.spigot.justlootit.JustLootItAccess;
import me.lauriichan.spigot.justlootit.nms.util.Vec3i;

public final class ConverterDataHelper {

    private ConverterDataHelper() {
        throw new UnsupportedOperationException();
    }

    public static void setOffset(PersistentDataContainer container, PersistentDataContainer otherContainer, Vec3i location,
        Vec3i otherLocation) {
        JustLootItAccess.setOffset(otherContainer, location.copy().subtract(otherLocation));
        JustLootItAccess.setOffset(container, otherLocation.copy().subtract(location));
    }

}
