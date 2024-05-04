package me.lauriichan.spigot.justlootit.compatibility.provider.betterstructures;

import org.bukkit.inventory.Inventory;

import com.magmaguy.betterstructures.config.generators.GeneratorConfig;
import com.magmaguy.betterstructures.config.generators.GeneratorConfigFields;

public class BetterStructuresAccess implements IBetterStructuresAccess {

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

}
