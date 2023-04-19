package me.lauriichan.spigot.justlootit.inventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.function.Consumer;

public class JustLootItInventory implements InventoryHolder {

    private final Inventory inventory;
    private Consumer<InventoryCloseEvent> closeAction;

    public JustLootItInventory(String title, int size) {
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    public void setCloseAction(Consumer<InventoryCloseEvent> closeAction) {
        this.closeAction = closeAction;
    }

    public Consumer<InventoryCloseEvent> getCloseAction() {
        return closeAction;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }
}
