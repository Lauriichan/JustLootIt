package me.lauriichan.spigot.justlootit.nms;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;

import net.md_5.bungee.api.chat.ItemTag;

public abstract class VersionHelper {

    public abstract VersionHandler handler();

    public abstract ItemTag asItemTag(ItemStack itemStack);

    public abstract net.md_5.bungee.api.chat.hover.content.Entity createEntityHover(Entity entity);
    
    public abstract List<NamespacedKey> getLootTables();

    public abstract void fill(Inventory inventory, Player player, Location location, LootTable lootTable, long seed);

}
