package me.lauriichan.spigot.justlootit.inventory.handler.loot;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import me.lauriichan.minecraft.pluginbase.inventory.ClickType;
import me.lauriichan.minecraft.pluginbase.inventory.DropType;
import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventory;
import me.lauriichan.minecraft.pluginbase.inventory.IHandler;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.config.MainConfig;
import me.lauriichan.spigot.justlootit.data.CachedInventory;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.storage.Stored;
import me.lauriichan.spigot.justlootit.util.BlockUtil;

public final class LootUIHandler implements IHandler {

    public static final LootUIHandler LOOT_HANDLER = new LootUIHandler();
    public static final String ATTR_ID = "PlayerStorageId";
    public static final String ATTR_LIDDED_LOCATION = "LiddedBlockLocation";

    public static final String PLAYER_DATA_LOOTING = "PlayerIsLooting";
    public static final int PLAYER_DATA_LOOTING_VALUE = 2;

    private final JustLootItPlugin plugin = JustLootItPlugin.get();
    private final VersionHandler versionHandler = plugin.versionHandler();

    private final MainConfig config = plugin.configManager().config(MainConfig.class);

    private LootUIHandler() {}

    @SuppressWarnings("unlikely-arg-type")
    @Override
    public boolean onEventClose(final HumanEntity entity, final IGuiInventory inventory) {
        inventory.setHandler(null);
        final Long id = inventory.attrUnset(ATTR_ID, Long.class);
        if (id == null) {
            return false;
        }
        final PlayerAdapter player = versionHandler.getPlayer(entity.getUniqueId());
        if (player == null) {
            return false;
        }
        player.removeData(PLAYER_DATA_LOOTING);
        final Location blockLocation = inventory.attrUnset(ATTR_LIDDED_LOCATION, Location.class);
        if (blockLocation != null) {
            player.versionHandler().platform().scheduler().regional(blockLocation,
                () -> BlockUtil.sendBlockClose(player.getLevel(), player.asBukkit(), blockLocation));
        }
        player.getCapability(StorageCapability.class).ifPresent(capability -> {
            Stored<CachedInventory> cached = capability.storage().read(id);
            if (cached != null && cached.value().equals(inventory.getInventory())) {
                return;
            }
            CachedInventory cachedInventory = new CachedInventory(inventory.getInventory());
            if (cached == null) {
                cached = capability.storage().registry().create(cachedInventory).id(id.longValue());
            } else {
                cached.value(cachedInventory);
            }
            try {
                capability.storage().write(cached);
            } catch (Throwable throwable) {
                plugin.logger().error("Failed to save cached inventory with id {0} for player {1} ({2})", throwable, id, entity.getName(),
                    entity.getUniqueId());
            }
        });
        return false;
    }
    
    @Override
    public boolean onEventClick(HumanEntity entity, IGuiInventory inventory, InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            return false;
        }
        return IHandler.super.onEventClick(entity, inventory, event);
    }

    @Override
    public boolean onEventDrag(HumanEntity entity, IGuiInventory inventory, InventoryDragEvent event) {
        return !config.allowItemStorageInContainer();
    }

    @Override
    public boolean onClickDrop(HumanEntity entity, IGuiInventory inventory, ItemStack item, int slot, int amount, DropType type) {
        return false;
    }

    @Override
    public boolean onClickPickup(HumanEntity entity, IGuiInventory inventory, ItemStack item, int slot, int amount, boolean cursor,
        ClickType type) {
        return false;
    }

    @Override
    public boolean onClickMove(HumanEntity entity, IGuiInventory inventory, Map<Integer, ItemStack> slots, ItemStack item, int amount) {
        return !config.allowItemStorageInContainer();
    }

    @Override
    public boolean onClickPlace(HumanEntity entity, IGuiInventory inventory, ItemStack item, int slot, int amount, ClickType type) {
        return !config.allowItemStorageInContainer();
    }

    @Override
    public boolean onClickSwap(HumanEntity entity, IGuiInventory inventory, ItemStack previous, ItemStack now, int slot, ClickType type) {
        return !config.allowItemStorageInContainer();
    }

}
