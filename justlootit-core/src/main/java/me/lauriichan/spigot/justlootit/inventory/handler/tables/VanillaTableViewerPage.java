package me.lauriichan.spigot.justlootit.inventory.handler.tables;

import org.bukkit.Material;
import org.bukkit.World;

import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.nms.VersionHelper;
import me.lauriichan.spigot.justlootit.util.CategorizedKeyMap;

@Extension
public class VanillaTableViewerPage extends LootTableViewerTabPage {

    private final VersionHelper helper;

    public VanillaTableViewerPage(JustLootItPlugin plugin) {
        this.helper = plugin.versionHelper();
    }

    @Override
    protected void provideLootTableKeys(World world, CategorizedKeyMap keyMap) {
        helper.getLootTables().forEach(keyMap::add);
    }

    @Override
    protected LootTableType tableType() {
        return LootTableType.VANILLA;
    }

    @Override
    protected ItemEditor createIcon(boolean selected) {
        return ItemEditor.of(Material.ENDER_CHEST);
    }

}
