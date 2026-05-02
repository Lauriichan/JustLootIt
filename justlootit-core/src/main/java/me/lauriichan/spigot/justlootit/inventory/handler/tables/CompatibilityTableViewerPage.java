package me.lauriichan.spigot.justlootit.inventory.handler.tables;

import org.bukkit.World;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.spigot.justlootit.compatibility.data.CompatibilityDataExtension;
import me.lauriichan.spigot.justlootit.inventory.Textures;
import me.lauriichan.spigot.justlootit.util.CategorizedKeyMap;

@Extension
public class CompatibilityTableViewerPage extends LootTableViewerTabPage {

    private final ObjectArrayList<CompatibilityDataExtension<?>> extensions;

    public CompatibilityTableViewerPage() {
        this.extensions = new ObjectArrayList<>(CompatibilityDataExtension.extensions());
    }

    @Override
    protected void provideLootTableKeys(World world, CategorizedKeyMap keyMap) {
        extensions.forEach(extension -> extension.provideLootTableKeys(world, keyMap));
    }

    @Override
    protected LootTableType tableType() {
        return LootTableType.COMPATIBILITY;
    }

    @Override
    protected ItemEditor createIcon(boolean selected) {
        return ItemEditor.ofHead(Textures.GEODE_SETTINGS);
    }

}
