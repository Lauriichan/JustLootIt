package me.lauriichan.spigot.justlootit.loot.condition;

import org.bukkit.NamespacedKey;

import me.lauriichan.spigot.justlootit.data.Container;
import me.lauriichan.spigot.justlootit.loot.ILootCondition;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;

public record LootTableCondition(NamespacedKey key) implements ILootCondition {

    @Override
    public boolean includes(Container container, PlayerAdapter player, NamespacedKey lootTableKey) {
        return lootTableKey != null && key.equals(lootTableKey);
    }

}