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
import net.md_5.bungee.api.chat.hover.content.Item;

public abstract class VersionHelper {

    public abstract VersionHandler handler();

    public final Item createItemHover(ItemStack itemStack) {
        return new Item(itemStack.getType().getKey().toString(), itemStack.getAmount(),
            ItemTag.ofNbt(handler().nbtHelper().asTag(itemStack).asString()));
    }

    public abstract net.md_5.bungee.api.chat.hover.content.Entity createEntityHover(Entity entity);

    public abstract List<NamespacedKey> getLootTables();

    public abstract LootTable getLootTable(NamespacedKey key);

    public abstract void fill(Inventory inventory, Player player, Location location, LootTable lootTable, long seed);

    public abstract int getItemFrameItemDataId();

    public boolean isTrialChamberBugged() {
        return false;
    }

    public abstract void triggerItemUsedCriteria(Player player, Location block, ItemStack bukkitStack);

    public abstract void triggerPiglins(Player player);

}
