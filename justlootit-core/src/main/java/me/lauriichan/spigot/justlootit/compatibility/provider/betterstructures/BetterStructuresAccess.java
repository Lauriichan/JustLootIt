package me.lauriichan.spigot.justlootit.compatibility.provider.betterstructures;

import org.bukkit.inventory.Inventory;

import com.magmaguy.betterstructures.config.generators.GeneratorConfig;
import com.magmaguy.betterstructures.config.generators.GeneratorConfigFields;
import com.magmaguy.betterstructures.config.treasures.TreasureConfig;
import com.magmaguy.betterstructures.config.treasures.TreasureConfigFields;

import me.lauriichan.spigot.justlootit.util.CategorizedKeyMap;

public class BetterStructuresAccess implements IBetterStructuresAccess {

    private final String pluginId;

    public BetterStructuresAccess(String pluginId) {
        this.pluginId = pluginId;
    }

    @Override
    public boolean hasLootForTreasureFile(String fileName) {
        TreasureConfigFields fields = TreasureConfig.getConfigFields(fileName);
        return fields != null && fields.getChestContents() != null;
    }

    @Override
    public boolean fillWithLootForTreasureFile(Inventory inventory, String fileName) {
        TreasureConfigFields fields = TreasureConfig.getConfigFields(fileName);
        if (fields == null || fields.getChestContents() == null) {
            return false;
        }
        fields.getChestContents().rollChestContents(new FakeBukkitContainer(inventory));
        return true;
    }

    @Override
    public boolean hasLootForGeneratorFile(String fileName) {
        GeneratorConfigFields fields = GeneratorConfig.getConfigFields(fileName);
        return fields != null && fields.getChestContents() != null;
    }

    @Override
    public boolean fillWithLootForGeneratorFile(Inventory inventory, String fileName) {
        GeneratorConfigFields fields = GeneratorConfig.getConfigFields(fileName);
        if (fields == null) {
            return false;
        }
        fields.getChestContents().rollChestContents(new FakeBukkitContainer(inventory));
        return true;
    }

    @Override
    public String migrateGeneratorFileToTreasureFile(String fileName) {
        GeneratorConfigFields fields = GeneratorConfig.getConfigFields(fileName);
        if (fields == null) {
            return null;
        }
        String treasureFile = fields.getTreasureFilename();
        if (treasureFile == null || treasureFile.isBlank()) {
            return null;
        }
        return treasureFile;
    }

    @Override
    public void provideLootTableKeys(CategorizedKeyMap keyMap) {
        TreasureConfig.getTreasureConfigurations().keySet().forEach(fileName -> keyMap.add(pluginId, fileName));
    }

}
