package me.lauriichan.spigot.justlootit.compatibility.provider.customstructures;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.loottables.LootTable;
import com.ryandw11.structure.loottables.LootTableType;
import com.ryandw11.structure.structure.Structure;
import com.ryandw11.structure.utils.RandomCollection;

public class CustomStructuresAccess implements ICustomStructuresAccess {

    private final CustomStructures plugin;

    public CustomStructuresAccess(Plugin plugin) {
        this.plugin = (CustomStructures) plugin;
    }

    @Override
    public boolean hasLootTable(String name, Material type) {
        Structure structure = plugin.getStructureHandler().getStructure(name);
        if (structure == null) {
            return false;
        }
        LootTableType tableType = LootTableType.valueOf(type);
        RandomCollection<LootTable> collection = structure.getLootTables(tableType == null ? LootTableType.CHEST : tableType);
        return collection != null && !collection.isEmpty();
    }

    @Override
    public boolean fillWithLootTable(Inventory inventory, Material type, Location location, String name, long seed) {
        LootTable table = getLootTable(name, type);
        if (table == null) {
            return false;
        }
        table.fillContainerInventory(inventory, new Random(seed), location);
        return true;
    }

    private LootTable getLootTable(String name, Material type) {
        Structure structure = plugin.getStructureHandler().getStructure(name);
        if (structure == null) {
            return null;
        }
        LootTableType tableType = LootTableType.valueOf(type);
        RandomCollection<LootTable> collection = structure.getLootTables(tableType == null ? LootTableType.CHEST : tableType);
        return collection == null ? null : collection.next();
    }

}
