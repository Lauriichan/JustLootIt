package me.lauriichan.spigot.justlootit.util;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import me.lauriichan.spigot.justlootit.util.registry.EntityRegistry;

public enum ExplosionType {

    UNKNOWN,
    BED,
    RESPAWN_ANCHOR,
    CREEPER,
    END_CRYSTAL,
    TNT,
    PLAYER_UNRECOGNISED_EXPLOSION;

    private final String configName;

    private ExplosionType() {
        this.configName = name().toLowerCase();
    }

    public String configName() {
        return configName;
    }

    public static ExplosionType fromBlock(Material material) {
        switch (material) {
        case WHITE_BED:
        case LIGHT_GRAY_BED:
        case GRAY_BED:
        case BLACK_BED:
        case BROWN_BED:
        case RED_BED:
        case ORANGE_BED:
        case YELLOW_BED:
        case LIME_BED:
        case GREEN_BED:
        case CYAN_BED:
        case LIGHT_BLUE_BED:
        case BLUE_BED:
        case PURPLE_BED:
        case MAGENTA_BED:
        case PINK_BED:
            return ExplosionType.BED;
        case RESPAWN_ANCHOR:
            return ExplosionType.RESPAWN_ANCHOR;
        default:
            return ExplosionType.UNKNOWN;
        }
    }

    public static ExplosionType fromEntity(EntityType entityType) {
        if (EntityRegistry.CREEPER.isValue(entityType)) {
            return ExplosionType.CREEPER;
        } else if (EntityRegistry.TNT.isValue(entityType)) {
            return ExplosionType.TNT;
        } else if (EntityRegistry.END_CRYSTAL.isValue(entityType)) {
            return ExplosionType.END_CRYSTAL;
        } else if (EntityRegistry.PLAYER.isValue(entityType)) {
            return ExplosionType.PLAYER_UNRECOGNISED_EXPLOSION;
        }
        return ExplosionType.UNKNOWN;
    }

}
