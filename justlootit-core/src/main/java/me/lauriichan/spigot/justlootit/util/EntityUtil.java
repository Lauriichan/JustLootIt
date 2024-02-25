package me.lauriichan.spigot.justlootit.util;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import me.lauriichan.spigot.justlootit.JustLootItFlag;

public final class EntityUtil {

    private EntityUtil() {
        throw new UnsupportedOperationException();
    }

    public static boolean isSuppportedEntity(Entity entity) {
        EntityType type = entity.getType();
        return type == EntityType.CHEST_BOAT || type == EntityType.MINECART_CHEST
            || JustLootItFlag.TILE_ENTITY_CONTAINERS.isSet() && type == EntityType.MINECART_HOPPER;
    }

}
