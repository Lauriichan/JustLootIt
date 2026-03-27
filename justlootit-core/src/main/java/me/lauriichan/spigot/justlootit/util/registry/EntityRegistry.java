package me.lauriichan.spigot.justlootit.util.registry;

import static me.lauriichan.spigot.justlootit.util.registry.KeyedWrapper.load;

import org.bukkit.Registry;
import org.bukkit.entity.EntityType;

import me.lauriichan.spigot.justlootit.JustLootItPlugin;

public final class EntityRegistry {

    public static final KeyedWrapper<EntityType> CHEST_BOAT;
    public static final KeyedWrapper<EntityType> MINECART_CHEST;
    public static final KeyedWrapper<EntityType> MINECART_HOPPER;
    public static final KeyedWrapper<EntityType> ITEM_FRAME;
    
    public static final KeyedWrapper<EntityType> CREEPER;
    public static final KeyedWrapper<EntityType> TNT;
    public static final KeyedWrapper<EntityType> END_CRYSTAL;
    public static final KeyedWrapper<EntityType> PLAYER;

    static {
        JustLootItPlugin plugin = JustLootItPlugin.get();
        Registry<EntityType> registry = Registry.ENTITY_TYPE;
        CHEST_BOAT = load(plugin, registry, "chest_boat", "oak_chest_boat", "spruce_chest_boat", "birch_chest_boat", "cherry_chest_boat",
            "pale_oak_chest_boat", "acacia_chest_boat", "mangrove_chest_boat", "jungle_chest_boat", "dark_oak_chest_boat",
            "bamboo_chest_raft", "dark_oak_chest_boat");
        MINECART_CHEST = load(plugin, registry, "chest_minecart");
        MINECART_HOPPER = load(plugin, registry, "hopper_minecart");
        ITEM_FRAME = load(plugin, registry, "item_frame", "glow_item_frame");
        CREEPER = load(plugin, registry, "creeper");
        TNT = load(plugin, registry, "tnt", "tnt_minecart");
        END_CRYSTAL = load(plugin, registry, "end_crystal");
        PLAYER = load(plugin, registry, "player");
    }

}
