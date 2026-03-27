package me.lauriichan.spigot.justlootit.util;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import me.lauriichan.spigot.justlootit.JustLootItFlag;
import me.lauriichan.spigot.justlootit.util.registry.EntityRegistry;

public final class EntityUtil {

    private EntityUtil() {
        throw new UnsupportedOperationException();
    }

    public static boolean isSupportedEntity(Entity entity) {
        return isSupportedEntity(entity.getType());
    }

    public static boolean isSupportedEntity(EntityType type) {
        return EntityRegistry.CHEST_BOAT.isValue(type) || EntityRegistry.MINECART_CHEST.isValue(type)
            || JustLootItFlag.TILE_ENTITY_CONTAINERS.isSet() && EntityRegistry.MINECART_HOPPER.isValue(type);
    }

    public static boolean isItemFrame(Entity entity) {
        return isItemFrame(entity.getType());
    }

    public static boolean isItemFrame(EntityType type) {
        return EntityRegistry.ITEM_FRAME.isValue(type);
    }

    public static int getInventorySize(EntityType type) {
        if (EntityRegistry.CHEST_BOAT.isValue(type) || EntityRegistry.MINECART_CHEST.isValue(type)) {
            return 27;
        } else if (EntityRegistry.MINECART_HOPPER.isValue(type)) {
            return 5;
        }
        return 0;
    }

}
