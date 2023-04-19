package me.lauriichan.spigot.justlootit.listener;

import me.lauriichan.spigot.justlootit.inventory.JustLootItInventory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryListener implements Listener {

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof JustLootItInventory inventory)) {
            return;
        }

        inventory.getCloseAction().accept(event);
    }
}
