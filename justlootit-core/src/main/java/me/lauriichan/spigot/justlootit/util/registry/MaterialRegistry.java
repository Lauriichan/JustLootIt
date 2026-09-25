package me.lauriichan.spigot.justlootit.util.registry;

import org.bukkit.Material;

public final class MaterialRegistry {

    public static final EnumWrapper<Material> BARREL = EnumWrapper.of(Material.BARREL);
    public static final EnumWrapper<Material> CHEST = EnumWrapper.of(Material.CHEST);
    public static final EnumWrapper<Material> TRAPPED_CHEST = EnumWrapper.of(Material.TRAPPED_CHEST);
    public static final EnumWrapper<Material> ENDER_CHEST = EnumWrapper.of(Material.ENDER_CHEST);
    public static final EnumWrapper<Material> SHULKER_BOX = EnumWrapper.of(new Material[] {
        Material.SHULKER_BOX,
        Material.BLACK_SHULKER_BOX,
        Material.WHITE_SHULKER_BOX,
        Material.RED_SHULKER_BOX,
        Material.GREEN_SHULKER_BOX,
        Material.LIME_SHULKER_BOX,
        Material.YELLOW_SHULKER_BOX,
        Material.GRAY_SHULKER_BOX,
        Material.LIGHT_GRAY_SHULKER_BOX,
        Material.CYAN_SHULKER_BOX,
        Material.LIGHT_BLUE_SHULKER_BOX,
        Material.BLUE_SHULKER_BOX,
        Material.PURPLE_SHULKER_BOX,
        Material.PINK_SHULKER_BOX,
        Material.MAGENTA_SHULKER_BOX,
        Material.ORANGE_SHULKER_BOX,
        Material.BROWN_SHULKER_BOX
    });

}
