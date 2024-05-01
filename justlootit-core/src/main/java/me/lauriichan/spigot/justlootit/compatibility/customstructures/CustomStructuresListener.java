package me.lauriichan.spigot.justlootit.compatibility.customstructures;

import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import com.ryandw11.structure.api.LootPopulateEvent;
import com.ryandw11.structure.loottables.LootTable;

import me.lauriichan.spigot.justlootit.JustLootItConstant;
import me.lauriichan.spigot.justlootit.JustLootItFlag;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;

public class CustomStructuresListener implements Listener {
    
    private final VersionHandler versionHandler;
    
    public CustomStructuresListener(final VersionHandler versionHandler) {
        this.versionHandler = versionHandler;
    }
    
    @EventHandler
    public void onLootPopulate(LootPopulateEvent event) {
        BlockState state = event.getLocation().getBlock().getState();
        if (!(state instanceof Container container)) {
            return;
        }
        Inventory inventory = container.getInventory();
        if (!JustLootItFlag.TILE_ENTITY_CONTAINERS.isSet()
            && JustLootItConstant.UNSUPPORTED_CONTAINER_TYPES.contains(inventory.getType())) {
            return;
        }
        LootTable table = event.getLootTable();
    }

    // TODO: Implement
    
}
