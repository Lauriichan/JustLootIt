package me.lauriichan.spigot.justlootit.loot.condition;

import org.bukkit.NamespacedKey;

import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.spigot.justlootit.data.Container;
import me.lauriichan.spigot.justlootit.loot.ILootCondition;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;

public record AndCondition(ObjectList<ILootCondition> conditions) implements ILootCondition {

    @Override
    public boolean includes(Container container, PlayerAdapter player, NamespacedKey lootTableKey) {
        for (ILootCondition condition : conditions) {
            if (!condition.includes(container, player, lootTableKey)) {
                return false;
            }
        }
        return true;
    }

}
