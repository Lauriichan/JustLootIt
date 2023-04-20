package me.lauriichan.spigot.justlootit.inventory;

import java.util.Map;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import me.lauriichan.spigot.justlootit.nms.VersionHandler;

public interface IHandler {

    default void onInit(final IGuiInventory inventory, final VersionHandler versionHandler) {}

    default void onUpdate(final IGuiInventory inventory, final VersionHandler versionHandler, final boolean changed) {}

    default boolean onEventClose(final HumanEntity entity, final IGuiInventory inventory, final VersionHandler versionHandler) {
        return false;
    }

    default boolean onEventOpen(final HumanEntity entity, final IGuiInventory inventory, final VersionHandler versionHandler) {
        return false;
    }

    default boolean onEventDrag(final HumanEntity entity, final IGuiInventory inventory, final VersionHandler versionHandler,
        final InventoryDragEvent event) {
        return false;
    }

    default boolean onEventMove(final HumanEntity entity /* Might be null if initiator is not a player */, final IGuiInventory inventory,
        final VersionHandler versionHandler, final boolean isDestination) {
        return false;
    }

    default boolean onEventClick(final HumanEntity entity, final IGuiInventory inventory, final VersionHandler versionHandler,
        final InventoryClickEvent event) {
        final InventoryAction action = event.getAction();
        final int slot = event.getSlot();
        ItemStack stack;
        switch (action) {
        case DROP_ALL_SLOT:
            if (event.getClickedInventory() != inventory.getInventory()) {
                return false;
            }
            return onClickDrop(entity, inventory, versionHandler, stack = inventory.get(slot), slot, stack.getAmount());
        case DROP_ONE_SLOT:
            if (event.getClickedInventory() != inventory.getInventory()) {
                return false;
            }
            return onClickDrop(entity, inventory, versionHandler, inventory.get(slot), slot, 1);
        case PICKUP_ALL:
            if (event.getClickedInventory() != inventory.getInventory()) {
                return false;
            }
            return onClickPickup(entity, inventory, versionHandler, stack = inventory.get(slot), slot, stack.getAmount(), true);
        case PICKUP_HALF:
            if (event.getClickedInventory() != inventory.getInventory()) {
                return false;
            }
            return onClickPickup(entity, inventory, versionHandler, stack = inventory.get(slot), slot, stack.getAmount() / 2, true);
        case PICKUP_ONE:
            if (event.getClickedInventory() != inventory.getInventory()) {
                return false;
            }
            return onClickPickup(entity, inventory, versionHandler, stack = inventory.get(slot), slot, 1, true);
        case PICKUP_SOME:
            if (event.getClickedInventory() != inventory.getInventory()) {
                return false;
            }
            int amount = event.getCursor().getMaxStackSize() - event.getCursor().getAmount();
            if (amount > (stack = inventory.get(slot)).getAmount()) {
                amount = stack.getAmount();
            }
            return onClickPickup(entity, inventory, versionHandler, stack, slot, amount, true);
        case PLACE_ALL:
            if (event.getClickedInventory() != inventory.getInventory()) {
                return false;
            }
            return onClickPlace(entity, inventory, versionHandler, event.getCursor(), slot, event.getCursor().getAmount());
        case PLACE_ONE:
            if (event.getClickedInventory() != inventory.getInventory()) {
                return false;
            }
            return onClickPlace(entity, inventory, versionHandler, event.getCursor(), slot, 1);
        case PLACE_SOME:
            if (event.getClickedInventory() != inventory.getInventory()) {
                return false;
            }
            return onClickPlace(entity, inventory, versionHandler, event.getCursor(), slot,
                (stack = inventory.get(slot)).getMaxStackSize() - inventory.get(slot).getAmount());
        case SWAP_WITH_CURSOR:
            if (event.getClickedInventory() != inventory.getInventory()) {
                return false;
            }
            return onClickSwap(entity, inventory, versionHandler, inventory.get(slot), event.getCursor(), slot);
        case HOTBAR_SWAP:
            if (event.getClickedInventory() != inventory.getInventory()) {
                return false;
            }
            return onClickSwap(entity, inventory, versionHandler, inventory.get(slot), event.getCurrentItem(), slot);
        case MOVE_TO_OTHER_INVENTORY:
            if (event.getClickedInventory() == inventory.getInventory()) {
                return onClickPickup(entity, inventory, versionHandler, event.getCurrentItem(), event.getSlot(),
                    event.getCurrentItem().getAmount(), false);
            } else {
                return onClickMove(entity, inventory, versionHandler, inventory.findPossibleSlots(event.getCurrentItem()),
                    event.getCurrentItem(), event.getCurrentItem().getAmount());
            }
        case CLONE_STACK:
            if (event.getClickedInventory() != inventory.getInventory()) {
                return false;
            }
            return onClickClone(entity, inventory, versionHandler, event.getCurrentItem(), slot);
        case COLLECT_TO_CURSOR:
            return true;
        default:
            return false;
        }
    }

    default boolean onClickClone(final HumanEntity entity, final IGuiInventory inventory, final VersionHandler versionHandler,
        final ItemStack item, final int slot) {
        return true;
    }

    default boolean onClickMove(final HumanEntity entity, final IGuiInventory inventory, final VersionHandler versionHandler,
        final Map<Integer, ItemStack> slots, final ItemStack item, final int amount) {
        return true;
    }

    default boolean onClickPickup(final HumanEntity entity, final IGuiInventory inventory, final VersionHandler versionHandler,
        final ItemStack item, final int slot, final int amount, final boolean cursor) {
        return true;
    }

    default boolean onClickPlace(final HumanEntity entity, final IGuiInventory inventory, final VersionHandler versionHandler,
        final ItemStack item, final int slot, final int amount) {
        return true;
    }

    default boolean onClickSwap(final HumanEntity entity, final IGuiInventory inventory, final VersionHandler versionHandler,
        final ItemStack previous, final ItemStack now, final int slot) {
        return true;
    }

    default boolean onClickDrop(final HumanEntity entity, final IGuiInventory inventory, final VersionHandler versionHandler,
        final ItemStack item, final int slot, final int amount) {
        return true;
    }

}
