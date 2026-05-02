package me.lauriichan.spigot.justlootit.inventory.handler.loot;

import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventory;
import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventoryUpdater;
import me.lauriichan.minecraft.pluginbase.inventory.paged.PagedInventoryHandler;
import me.lauriichan.spigot.justlootit.data.Container;
import me.lauriichan.spigot.justlootit.data.IInventoryContainer;
import me.lauriichan.spigot.justlootit.data.IInventoryContainer.IResult;
import me.lauriichan.spigot.justlootit.message.UIInventoryNames;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.util.InventoryUtil;

public final class GeneratedLootUIHandler extends BaseLootUIHandler {

    public static final GeneratedLootUIHandler LOOT_HANDLER = new GeneratedLootUIHandler();

    public static final String ATTR_CONTAINER = "Container";
    public static final String ATTR_INVENTORY_HOLDER = "InventoryHolder";
    public static final String ATTR_LOCATION = "Location";

    private GeneratedLootUIHandler() {}

    @Override
    public void onInit(HumanEntity entity, IGuiInventory inventory) {
        if (!inventory.attrHas(ATTR_INVENTORY_HOLDER)) {
            return;
        }
        final Actor<HumanEntity> actor = plugin.actor(entity);
        final Inventory holderInventory = inventory.attr(ATTR_INVENTORY_HOLDER, InventoryHolder.class).getInventory();
        final InventoryType type = holderInventory.getType();
        int columnAmount = IGuiInventory.getColumnAmount(type);
        IGuiInventoryUpdater updater = inventory.updater().title(actor.getTranslatedMessageAsString(UIInventoryNames.LOOT_UI_NAME));
        if (columnAmount == 9) {
            updater.chestSize(CHEST_VALUES[(InventoryUtil.getSize(holderInventory) / columnAmount) - 1]);
        } else {
            updater.type(type);
        }
        inventory.attrSet(PagedInventoryHandler.PLAYER_PROPERTY, entity);
        if (!updater.apply()) {
            inventory.update();
        }
    }

    @Override
    public void onUpdate(IGuiInventory inventory, boolean changed) {
        if (!inventory.attrHas(ATTR_INVENTORY_HOLDER)) {
            return;
        }
        InventoryHolder holder = inventory.attrUnset(ATTR_INVENTORY_HOLDER, InventoryHolder.class);
        Container container = inventory.attrUnset(ATTR_CONTAINER, Container.class);
        Location location = inventory.attrUnset(ATTR_LOCATION, Location.class);

        PlayerAdapter player = versionHandler.getPlayer(inventory.attrUnset(PagedInventoryHandler.PLAYER_PROPERTY, Player.class));
        IResult result = ((IInventoryContainer) container).fill(player, holder, location, inventory.getInventory());
        if (result.isFailed()) {
            // Reset access, we failed to fill
            container.decreaseAccessCount(player.getUniqueId());
            return;
        }
        ((IInventoryContainer) container).awaitProvidedEvent(player, inventory, holder, location, result);
    }

}
