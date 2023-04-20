package me.lauriichan.spigot.justlootit.listener;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.scheduler.BukkitScheduler;

import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.inventory.IGuiInventory;

public final class GuiListener implements Listener {

    private final JustLootItPlugin plugin;
    private final BukkitScheduler scheduler = Bukkit.getScheduler();

    public GuiListener(final JustLootItPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(final InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof final IGuiInventory inventory && inventory.hasHandler()) {
            event.setCancelled(inventory.getHandler().onEventClick(event.getWhoClicked(), inventory, plugin.versionHandler(), event));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(final InventoryMoveItemEvent event) {
        if (event.getSource().getHolder() instanceof final IGuiInventory inventory && inventory.hasHandler()) {
            final List<HumanEntity> viewers = event.getInitiator().getViewers();
            event.setCancelled(
                inventory.getHandler().onEventMove(viewers.isEmpty() ? null : viewers.get(0), inventory, plugin.versionHandler(), false));
        } else if (event.getDestination().getHolder() instanceof final IGuiInventory inventory && inventory.hasHandler()) {
            final List<HumanEntity> viewers = event.getInitiator().getViewers();
            event.setCancelled(
                inventory.getHandler().onEventMove(viewers.isEmpty() ? null : viewers.get(0), inventory, plugin.versionHandler(), true));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(final InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof final IGuiInventory inventory && inventory.hasHandler()) {
            event.setCancelled(inventory.getHandler().onEventDrag(event.getWhoClicked(), inventory, plugin.versionHandler(), event));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClose(final InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof final IGuiInventory inventory && inventory.hasHandler()) {
            if (inventory.getHandler().onEventClose(event.getPlayer(), inventory, plugin.versionHandler())) {
                scheduler.runTask(plugin, () -> event.getPlayer().openInventory(inventory.getInventory()));
            } else {
                // Clear inventory on close to free up space
                inventory.clear();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onOpen(final InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof final IGuiInventory inventory && inventory.hasHandler()) {
            event.setCancelled(inventory.getHandler().onEventOpen(event.getPlayer(), inventory, plugin.versionHandler()));
        }
    }

}
