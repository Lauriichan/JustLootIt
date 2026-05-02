package me.lauriichan.spigot.justlootit.compatibility.provider.customstructures;

import java.util.Map;
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

import me.lauriichan.spigot.justlootit.util.CategorizedKeyMap;

public class CustomStructuresAccess implements ICustomStructuresAccess {

    private final CustomStructures plugin;
    private final Map<String, LootTable> lootTableMap;

    public CustomStructuresAccess(Plugin plugin) {
        this.plugin = (CustomStructures) plugin;
        this.lootTableMap = this.plugin.getLootTableHandler().getLootTables();
    }

    @Override
    public boolean hasLootTable(String name) {
        return lootTableMap.containsKey(name);
    }

    @Override
    public boolean fillWithLootTable(Inventory inventory, Location location, String name, long seed) {
        LootTable table = lootTableMap.get(name);
        if (table == null) {
            return false;
        }
        table.fillContainerInventory(inventory, new Random(seed), location);
        return true;
    }

    @Override
    public boolean hasStructureLootTable(String name, Material type) {
        Structure structure = plugin.getStructureHandler().getStructure(name);
        if (structure == null) {
            return false;
        }
        LootTableType tableType = LootTableType.valueOf(type);
        RandomCollection<LootTable> collection = structure.getLootTables(tableType == null ? LootTableType.CHEST : tableType);
        return collection != null && !collection.isEmpty();
    }

    @Override
    public boolean fillWithStructureLootTable(Inventory inventory, Material type, Location location, String name, long seed) {
        LootTable table = getStructureLootTable(name, type);
        if (table == null) {
            return false;
        }
        table.fillContainerInventory(inventory, new Random(seed), location);
        return true;
    }

    private LootTable getStructureLootTable(String name, Material type) {
        Structure structure = plugin.getStructureHandler().getStructure(name);
        if (structure == null) {
            return null;
        }
        LootTableType tableType = LootTableType.valueOf(type);
        RandomCollection<LootTable> collection = structure.getLootTables(tableType == null ? LootTableType.CHEST : tableType);
        return collection == null ? null : collection.next();
    }

    @Override
    public void provideLootTableKeys(CategorizedKeyMap keyMap) {
        final String namespace = plugin.getName();
        lootTableMap.keySet().forEach(lootTable -> keyMap.add(namespace, lootTable));
    }

}
