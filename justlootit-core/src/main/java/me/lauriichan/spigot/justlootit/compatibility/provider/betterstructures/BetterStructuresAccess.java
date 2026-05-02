package me.lauriichan.spigot.justlootit.compatibility.provider.betterstructures;

import org.bukkit.inventory.Inventory;

import com.magmaguy.betterstructures.config.generators.GeneratorConfig;
import com.magmaguy.betterstructures.config.generators.GeneratorConfigFields;

import me.lauriichan.spigot.justlootit.util.CategorizedKeyMap;

public class BetterStructuresAccess implements IBetterStructuresAccess {
    
    private final String pluginId;
    
    public BetterStructuresAccess(String pluginId) {
        this.pluginId = pluginId;
    }

    @Override
    public boolean hasLootForFile(String fileName) {
        GeneratorConfigFields fields = GeneratorConfig.getConfigFields(fileName);
        return fields != null && fields.getChestContents() != null;
    }

    @Override
    public boolean fillWithLootForFile(Inventory inventory, String fileName) {
        GeneratorConfigFields fields = GeneratorConfig.getConfigFields(fileName);
        if (fields == null || fields.getChestContents() == null) {
            return false;
        }
        fields.getChestContents().rollChestContents(new FakeBukkitContainer(inventory));
        return true;
    }
    
    @Override
    public void provideLootTableKeys(CategorizedKeyMap keyMap) {
        GeneratorConfig.getGeneratorConfigurations().keySet().forEach(fileName -> keyMap.add(pluginId, fileName));
    }

}
