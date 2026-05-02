package me.lauriichan.spigot.justlootit.inventory.handler.tables;

import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.inventory.AbstractPageHandler;

@Extension
public class LootTableViewerHandler extends AbstractPageHandler<LootTableViewerPage> {

    public LootTableViewerHandler(JustLootItPlugin plugin) {
        super(plugin, LootTableViewerPage.class);
    }

}
