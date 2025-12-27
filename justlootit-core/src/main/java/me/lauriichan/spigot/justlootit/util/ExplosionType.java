package me.lauriichan.spigot.justlootit.util;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

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
        switch (entityType) {
        case CREEPER:
            return ExplosionType.CREEPER;
        case PRIMED_TNT:
        case MINECART_TNT:
            return ExplosionType.TNT;
        case ENDER_CRYSTAL:
            return ExplosionType.END_CRYSTAL;
        case PLAYER:
            return ExplosionType.PLAYER_UNRECOGNISED_EXPLOSION;
        default:
            return ExplosionType.UNKNOWN;
        }
    }

}
