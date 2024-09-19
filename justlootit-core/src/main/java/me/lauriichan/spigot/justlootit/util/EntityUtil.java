package me.lauriichan.spigot.justlootit.util;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import me.lauriichan.spigot.justlootit.JustLootItFlag;

public final class EntityUtil {

    private EntityUtil() {
        throw new UnsupportedOperationException();
    }

    public static boolean isSupportedEntity(Entity entity) {
        return isSupportedEntity(entity.getType());
    }

    public static boolean isSupportedEntity(EntityType type) {
        return type == EntityType.CHEST_BOAT || type == EntityType.MINECART_CHEST
            || JustLootItFlag.TILE_ENTITY_CONTAINERS.isSet() && type == EntityType.MINECART_HOPPER;
    }

    public static boolean isItemFrame(Entity entity) {
        return isItemFrame(entity.getType());
    }

    public static boolean isItemFrame(EntityType type) {
        return type == EntityType.ITEM_FRAME || type == EntityType.GLOW_ITEM_FRAME;
    }
    
    public static int getInventorySize(EntityType type) {
        if (type == EntityType.CHEST_BOAT || type == EntityType.MINECART_CHEST) {
            return 27;
        } else if (type == EntityType.MINECART_HOPPER) {
            return 5;
        }
        return 0;
    }

}
