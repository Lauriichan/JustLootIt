package me.lauriichan.spigot.justlootit.loot.condition;

import org.bukkit.NamespacedKey;

import me.lauriichan.spigot.justlootit.data.Container;
import me.lauriichan.spigot.justlootit.data.ContainerType;
import me.lauriichan.spigot.justlootit.loot.ILootCondition;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;

public record TypeCondition(ContainerType type) implements ILootCondition {

    @Override
    public boolean includes(Container container, PlayerAdapter player, NamespacedKey lootTableKey) {
        return container.type() == type;
    }

}
