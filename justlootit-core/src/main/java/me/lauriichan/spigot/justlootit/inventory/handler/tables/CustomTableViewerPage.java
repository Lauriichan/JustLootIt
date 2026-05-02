package me.lauriichan.spigot.justlootit.inventory.handler.tables;

import org.bukkit.Material;
import org.bukkit.World;

import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.config.loot.LootTableDirectoryData;
import me.lauriichan.spigot.justlootit.util.CategorizedKeyMap;

@Extension
public class CustomTableViewerPage extends LootTableViewerTabPage {

    private final LootTableDirectoryData lootTables;

    public CustomTableViewerPage(JustLootItPlugin plugin) {
        this.lootTables = plugin.dataManager().directoryData(LootTableDirectoryData.class);
    }
    
    @Override
    public boolean defaultPage() {
        return true;
    }

    @Override
    protected void provideLootTableKeys(World world, CategorizedKeyMap keyMap) {
        lootTables.getTables().forEach(table -> keyMap.add(table.getKey()));
    }

    @Override
    protected LootTableType tableType() {
        return LootTableType.CUSTOM;
    }

    @Override
    protected ItemEditor createIcon(boolean selected) {
        return ItemEditor.of(Material.BUDDING_AMETHYST);
    }

}
