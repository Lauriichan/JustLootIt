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
        JustLootItAccess.setOffset(otherContainer, calculateOffset(otherLocation, location));
        JustLootItAccess.setOffset(container, calculateOffset(location, otherLocation));
    }
    
    public static Vec3i calculateOffset(Vec3i origin, Vec3i other) {
        return other.copy().subtract(origin);
    }

}
