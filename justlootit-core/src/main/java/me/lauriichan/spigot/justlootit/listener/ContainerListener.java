package me.lauriichan.spigot.justlootit.listener;

import java.time.Duration;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Lidded;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.inventory.ChestSize;
import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventory;
import me.lauriichan.minecraft.pluginbase.listener.IListenerExtension;
import me.lauriichan.spigot.justlootit.JustLootItConstant;
import me.lauriichan.spigot.justlootit.JustLootItFlag;
import me.lauriichan.spigot.justlootit.JustLootItKey;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.capability.PlayerGUICapability;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.data.CacheLookupTable;
import me.lauriichan.spigot.justlootit.data.CacheLookupTable.WorldEntry;
import me.lauriichan.spigot.justlootit.inventory.handler.LootUIHandler;
import me.lauriichan.spigot.justlootit.data.CachedInventory;
import me.lauriichan.spigot.justlootit.data.Container;
import me.lauriichan.spigot.justlootit.data.IInventoryContainer;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.storage.IStorage;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.util.BlockUtil;
import me.lauriichan.spigot.justlootit.util.InventoryUtil;
import me.lauriichan.spigot.justlootit.util.SimpleDataType;

@Extension
public class ContainerListener implements IListenerExtension {

    private final VersionHandler versionHandler;

    public ContainerListener(final JustLootItPlugin plugin) {
        this.versionHandler = plugin.versionHandler();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInteract(final PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        final Block block = event.getClickedBlock();
        final BlockState state = block.getState();
        if (!(state instanceof org.bukkit.block.Container)) {
            return;
        }
        final BlockData blockData = state.getBlockData();
        final org.bukkit.block.Container container = (org.bukkit.block.Container) state;
        final PersistentDataContainer dataContainer = container.getPersistentDataContainer();
        if (blockData instanceof Chest chest && chest.getType() != Type.SINGLE) {
            if (!dataContainer.has(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR)) {
                return;
            }
            Vector offset = dataContainer.get(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR);
            BlockState otherState = block.getWorld()
                .getBlockAt(block.getX() + offset.getBlockX(), block.getY(), block.getZ() + offset.getBlockZ()).getState();
            if (!(otherState instanceof org.bukkit.block.Container otherContainer)) {
                dataContainer.remove(JustLootItKey.chestData());
                state.update(true);
                return;
            }
            PersistentDataContainer otherDataContainer = otherContainer.getPersistentDataContainer();
            org.bukkit.block.Container accessContainer = dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG) ? container
                : otherContainer;
            if (accessContainer != container && !otherDataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                otherDataContainer.remove(JustLootItKey.chestData());
                dataContainer.remove(JustLootItKey.chestData());
                otherContainer.update(true);
                container.update(true);
                return;
            }
            PersistentDataContainer accessDataContainer = accessContainer == container ? dataContainer : otherDataContainer;
            accessContainer(accessContainer.getLocation(), accessContainer, accessDataContainer, event, event.getPlayer(),
                accessDataContainer.get(JustLootItKey.identity(), PersistentDataType.LONG));
            if (!accessDataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                otherDataContainer.remove(JustLootItKey.chestData());
                dataContainer.remove(JustLootItKey.chestData());
                accessDataContainer.remove(JustLootItKey.identity());
                otherContainer.update(true);
                container.update(true);
            }
            return;
        }
        if (!dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG) || !JustLootItFlag.TILE_ENTITY_CONTAINERS.isSet()
            && JustLootItConstant.UNSUPPORTED_CONTAINER_TYPES.contains(container.getInventory().getType())) {
            return;
        }
        accessContainer(block.getLocation(), container, dataContainer, event, event.getPlayer(),
            dataContainer.get(JustLootItKey.identity(), PersistentDataType.LONG));
        if (!dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
            container.update();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInteractEntity(final PlayerInteractAtEntityEvent event) {
        final Entity entity = event.getRightClicked();
        final EntityType type = entity.getType();
        if (type != EntityType.MINECART_CHEST && type != EntityType.MINECART_HOPPER && type != EntityType.CHEST_BOAT
            || !JustLootItFlag.TILE_ENTITY_CONTAINERS.isSet() && type == EntityType.MINECART_HOPPER) {
            return;
        }
        final PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
        if (!dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
            return;
        }
        accessContainer(entity.getLocation(), (InventoryHolder) entity, dataContainer, event, event.getPlayer(),
            dataContainer.get(JustLootItKey.identity(), PersistentDataType.LONG));
    }

    private void accessContainer(final Location location, final InventoryHolder inventoryHolder, final PersistentDataContainer data,
        final Cancellable event, final Player bukkitPlayer, final long id) {
        final WorldEntry entryId = new WorldEntry(location.getWorld(), id);
        final PlayerAdapter player = versionHandler.getPlayer(bukkitPlayer);
        final UUID playerId = bukkitPlayer.getUniqueId();
        versionHandler.getLevel(location.getWorld()).getCapability(StorageCapability.class).ifPresentOrElse(capability -> {
            final Container dataContainer = (Container) capability.storage().read(id);
            if (dataContainer == null) {
                data.remove(JustLootItKey.identity());
                return;
            }
            event.setCancelled(true);
            player.getCapability(StorageCapability.class).ifPresent(playerCapability -> {
                final IStorage<Storable> playerStorage = playerCapability.storage();
                final CacheLookupTable lookupTable = CacheLookupTable.retrieve(playerStorage);
                if (!dataContainer.access(playerId)) {
                    if (lookupTable.access(entryId)) {
                        final CachedInventory cachedInventory = (CachedInventory) playerStorage
                            .read(lookupTable.getEntryIdByMapped(entryId));
                        final int rowSize = IGuiInventory.getRowSize(cachedInventory.getType());
                        if (cachedInventory.size() % rowSize == 0) {
                            final int columnAmount = cachedInventory.size() / rowSize;
                            if (((rowSize != 9) || ((columnAmount <= 6) && (columnAmount >= 1)))) {
                                player.getCapability(PlayerGUICapability.class).ifPresent(guiCapability -> {
                                    final IGuiInventory inventory = guiCapability.gui();
                                    if (rowSize == 9) {
                                        inventory.setChestSize(ChestSize.values()[columnAmount - 1]);
                                    } else {
                                        inventory.setType(cachedInventory.getType());
                                    }
                                    inventory.getInventory().setContents(cachedInventory.getItems());
                                    inventory.attrSet(LootUIHandler.ATTR_ID, cachedInventory.id());
                                    inventory.setHandler(LootUIHandler.LOOT_HANDLER);
                                    inventory.open(bukkitPlayer);
                                    if (inventoryHolder instanceof DoubleChest || inventoryHolder instanceof Lidded) {
                                        inventory.attrSet(LootUIHandler.ATTR_LIDDED_LOCATION, location);
                                        BlockUtil.sendBlockOpen(bukkitPlayer, location);
                                    }
                                });
                                return;
                            }
                        }
                        playerStorage.delete(cachedInventory.id());
                    }

                    final Duration duration = dataContainer.durationUntilNextAccess(playerId);
                    if (duration.isNegative()) {
                        // TODO: Send message, never accessible
                        return;
                    }
                    // TODO: Send message, not accessible yet
                    return;
                }

                player.getCapability(PlayerGUICapability.class).ifPresent(guiCapability -> {
                    final IGuiInventory inventory = guiCapability.gui();
                    if (!(dataContainer instanceof final IInventoryContainer container)) {
                        // Do nothing, no need to allocate anything if we have no inventory
                        return;
                    }
                    final Inventory holderInventory = inventoryHolder.getInventory();
                    int rowSize = IGuiInventory.getRowSize(holderInventory.getType());
                    if (rowSize == 9) {
                        inventory.setChestSize(ChestSize.values()[(InventoryUtil.getSize(holderInventory) / rowSize) - 1]);
                    } else {
                        inventory.setType(holderInventory.getType());
                    }
                    container.fill(player, location, inventory.getInventory());
                    inventory.attrSet(LootUIHandler.ATTR_ID, lookupTable.acquire(entryId));
                    inventory.setHandler(LootUIHandler.LOOT_HANDLER);
                    inventory.open(bukkitPlayer);
                    if (inventoryHolder instanceof DoubleChest || inventoryHolder instanceof Lidded) {
                        inventory.attrSet(LootUIHandler.ATTR_LIDDED_LOCATION, location);
                        BlockUtil.sendBlockOpen(bukkitPlayer, location);
                    }
                });
            });
        }, () -> event.setCancelled(true));
    }

}
