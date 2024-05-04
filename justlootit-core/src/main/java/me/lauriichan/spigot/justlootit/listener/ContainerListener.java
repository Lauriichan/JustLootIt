package me.lauriichan.spigot.justlootit.listener;

import java.time.Duration;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Lidded;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.lauriichan.laylib.localization.Key;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.inventory.ChestSize;
import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventory;
import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventoryUpdater;
import me.lauriichan.minecraft.pluginbase.listener.IListenerExtension;
import me.lauriichan.spigot.justlootit.JustLootItConstant;
import me.lauriichan.spigot.justlootit.JustLootItFlag;
import me.lauriichan.spigot.justlootit.JustLootItKey;
import me.lauriichan.spigot.justlootit.JustLootItPermission;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.capability.PlayerGUICapability;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.command.impl.LootItActor;
import me.lauriichan.spigot.justlootit.config.MainConfig;
import me.lauriichan.spigot.justlootit.data.CacheLookupTable;
import me.lauriichan.spigot.justlootit.data.CachedInventory;
import me.lauriichan.spigot.justlootit.data.Container;
import me.lauriichan.spigot.justlootit.data.IInventoryContainer;
import me.lauriichan.spigot.justlootit.data.CacheLookupTable.WorldEntry;
import me.lauriichan.spigot.justlootit.inventory.handler.loot.LootUIHandler;
import me.lauriichan.spigot.justlootit.message.Messages;
import me.lauriichan.spigot.justlootit.message.UIInventoryNames;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.util.Vec3i;
import me.lauriichan.spigot.justlootit.storage.IStorage;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.util.BlockUtil;
import me.lauriichan.spigot.justlootit.util.DataHelper;
import me.lauriichan.spigot.justlootit.util.EntityUtil;
import me.lauriichan.spigot.justlootit.util.InventoryUtil;
import me.lauriichan.spigot.justlootit.util.SimpleDataType;

@Extension
public class ContainerListener implements IListenerExtension {

    private static final ChestSize[] CHEST_VALUES = ChestSize.values();

    private final JustLootItPlugin plugin;
    private final MainConfig config;

    public ContainerListener(final JustLootItPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.configManager().config(MainConfig.class);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPlaceEvent(final BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (!(block.getState() instanceof org.bukkit.block.Container container) || !(container.getBlockData() instanceof Chest chest)
            || chest.getType() == Type.SINGLE) {
            return;
        }
        org.bukkit.block.Container otherContainer = BlockUtil.findChestAround(block.getWorld(), block.getLocation(), chest.getType(),
            chest.getFacing());
        if (otherContainer == null || !otherContainer.getPersistentDataContainer().has(JustLootItKey.identity(), PersistentDataType.LONG)) {
            return;
        }
        Chest cloned = (Chest) chest.clone();
        cloned.setType(Type.SINGLE);
        container.setBlockData(cloned);
        container.update(false, false);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockBreakEvent(final BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!(block.getState() instanceof org.bukkit.block.Container container)) {
            return;
        }
        PersistentDataContainer dataContainer = container.getPersistentDataContainer();
        if (!dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)
            && !dataContainer.has(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR)) {
            return;
        }
        org.bukkit.block.Container otherContainer = null;
        if (container.getBlockData() instanceof Chest chest && chest.getType() != Type.SINGLE) {
            Vec3i offset = dataContainer.get(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR);
            BlockState otherState = block.getWorld().getBlockState(offset.addOn(block.getLocation()));
            if (!(otherState instanceof org.bukkit.block.Container)) {
                dataContainer.remove(JustLootItKey.chestData());
                container.update(false, false);
                return;
            }
            otherContainer = ((org.bukkit.block.Container) otherState);
            if (!dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                PersistentDataContainer otherDataContainer = otherContainer.getPersistentDataContainer();
                if (!otherDataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                    otherDataContainer.remove(JustLootItKey.chestData());
                    dataContainer.remove(JustLootItKey.chestData());
                    otherContainer.update(false, false);
                    container.update(false, false);
                    return;
                }
            }
        } else if(!dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
            dataContainer.remove(JustLootItKey.chestData());
            container.update(false, false);
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        LootItActor<?> actor = plugin.actor(player);
        if (!player.hasPermission(JustLootItPermission.ACTION_REMOVE_CONTAINER_BLOCK)) {
            actor.sendTranslatedMessage(Messages.CONTAINER_BREAK_UNPERMITTED_BLOCK);
            return;
        }
        if (!player.isSneaking()) {
            actor.sendTranslatedMessage(Messages.CONTAINER_BREAK_PERMITTED_BLOCK);
            return;
        }
        if (!DataHelper.canBreakContainer(dataContainer, actor.getId())) {
            container.update(false, false);
            actor.sendTranslatedMessage(Messages.CONTAINER_BREAK_CONFIRMATION_BLOCK);
            return;
        }
        if (otherContainer != null) {
            if (!dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                event.setCancelled(false);
                otherContainer.getPersistentDataContainer().remove(JustLootItKey.chestData());
                dataContainer.remove(JustLootItKey.chestData());
                container.update(false, false);
                Chest chest = (Chest) otherContainer.getBlockData();
                chest.setType(Type.SINGLE);
                otherContainer.setBlockData(chest);
                otherContainer.update(true, false);
                actor.sendTranslatedBarMessage(Messages.CONTAINER_BREAK_DOUBLE_CHEST);
                return;
            }
            otherContainer.getPersistentDataContainer().remove(JustLootItKey.chestData());
            Chest chest = (Chest) otherContainer.getBlockData();
            chest.setType(Type.SINGLE);
            otherContainer.setBlockData(chest);
            otherContainer.update(true, false);
            container.setBlockData(chest);
        }
        dataContainer.remove(JustLootItKey.breakData());
        final long id = dataContainer.get(JustLootItKey.identity(), PersistentDataType.LONG);
        dataContainer.remove(JustLootItKey.identity());
        dataContainer.remove(JustLootItKey.breakData());
        container.update(true, false);
        actor.sendTranslatedMessage(Messages.CONTAINER_BREAK_REMOVED_BLOCK, Key.of("id", id));
        if (!config.deleteOnBreak()) {
            return;
        }
        actor.versionHandler().getLevel(player.getWorld()).getCapability(StorageCapability.class).ifPresent(capability -> {
            if (!capability.storage().delete(id)) {
                actor.sendTranslatedMessage(Messages.CONTAINER_BREAK_NO_CONTAINER, Key.of("id", id));
            }
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onVehicleDamage(final VehicleDamageEvent event) {
        Vehicle vehicle = event.getVehicle();
        if (!EntityUtil.isSuppportedEntity(vehicle)) {
            return;
        }
        PersistentDataContainer dataContainer = vehicle.getPersistentDataContainer();
        if (!dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
            return;
        }
        event.setCancelled(true);
        Entity attacker = event.getAttacker();
        if (attacker == null || attacker.getType() != EntityType.PLAYER) {
            return;
        }
        Player player = (Player) attacker;
        LootItActor<?> actor = plugin.actor(player);
        if (!player.hasPermission(JustLootItPermission.ACTION_REMOVE_CONTAINER_ENTITY)) {
            actor.sendTranslatedMessage(Messages.CONTAINER_BREAK_UNPERMITTED_ENTITY);
            return;
        }
        if (!player.isSneaking()) {
            actor.sendTranslatedMessage(Messages.CONTAINER_BREAK_PERMITTED_ENTITY);
            return;
        }
        if (!DataHelper.canBreakContainer(dataContainer, actor.getId())) {
            actor.sendTranslatedMessage(Messages.CONTAINER_BREAK_CONFIRMATION_ENTITY);
            return;
        }
        final long id = dataContainer.get(JustLootItKey.identity(), PersistentDataType.LONG);
        dataContainer.remove(JustLootItKey.identity());
        dataContainer.remove(JustLootItKey.breakData());
        actor.sendTranslatedMessage(Messages.CONTAINER_BREAK_REMOVED_ENTITY, Key.of("id", id));
        if (!config.deleteOnBreak()) {
            return;
        }
        actor.versionHandler().getLevel(player.getWorld()).getCapability(StorageCapability.class).ifPresent(capability -> {
            if (!capability.storage().delete(id)) {
                actor.sendTranslatedMessage(Messages.CONTAINER_BREAK_NO_CONTAINER, Key.of("id", id));
            }
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInteract(final PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        final Player player = event.getPlayer();
        if (player.isSneaking()) {
            final PlayerInventory inventory = player.getInventory();
            final ItemStack first = inventory.getItemInMainHand(), second = inventory.getItemInOffHand();
            if (first.getType().isBlock() && !first.getType().isAir() || second.getType().isBlock() && !second.getType().isAir()) {
                return;
            }
        }
        final Block block = event.getClickedBlock();
        final BlockState state = block.getState();
        if (!(state instanceof org.bukkit.block.Container container)) {
            return;
        }
        final PersistentDataContainer dataContainer = container.getPersistentDataContainer();
        if (!dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
            if (!(container.getBlockData() instanceof Chest chest) || chest.getType() == Type.SINGLE
                || !dataContainer.has(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR)) {
                return;
            }
            Vec3i offset = dataContainer.get(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR);
            BlockState otherState = block.getWorld().getBlockAt(offset.addOn(block.getLocation())).getState();
            if (!(otherState instanceof org.bukkit.block.Container otherContainer)) {
                dataContainer.remove(JustLootItKey.chestData());
                state.update(false, false);
                return;
            }
            PersistentDataContainer otherDataContainer = otherContainer.getPersistentDataContainer();
            if (!otherDataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                otherDataContainer.remove(JustLootItKey.chestData());
                dataContainer.remove(JustLootItKey.chestData());
                otherContainer.update(false, false);
                container.update(false, false);
                return;
            }
            accessContainer(otherContainer.getLocation(), otherContainer, otherDataContainer, event, event.getPlayer(),
                otherDataContainer.get(JustLootItKey.identity(), PersistentDataType.LONG));
            if (otherDataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                return;
            }
            otherDataContainer.remove(JustLootItKey.chestData());
            dataContainer.remove(JustLootItKey.chestData());
            otherContainer.update(false, false);
            container.update(false, false);
            return;
        }
        if (!JustLootItFlag.TILE_ENTITY_CONTAINERS.isSet()
            && JustLootItConstant.UNSUPPORTED_CONTAINER_TYPES.contains(container.getInventory().getType())) {
            return;
        }
        accessContainer(block.getLocation(), container, dataContainer, event, event.getPlayer(),
            dataContainer.get(JustLootItKey.identity(), PersistentDataType.LONG));
        if (dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
            return;
        }
        container.update(false, false);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInteractEntity(final PlayerInteractEntityEvent event) {
        final Entity entity = event.getRightClicked();
        if (!EntityUtil.isSuppportedEntity(entity)) {
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
        final LootItActor<?> actor = plugin.actor(bukkitPlayer);
        final PlayerAdapter player = actor.versionHandler().getPlayer(bukkitPlayer);
        final UUID playerId = bukkitPlayer.getUniqueId();
        actor.versionHandler().getLevel(location.getWorld()).getCapability(StorageCapability.class).ifPresentOrElse(capability -> {
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
                        final int columnAmount = IGuiInventory.getColumnAmount(cachedInventory.getType());
                        if (cachedInventory.size() % columnAmount == 0) {
                            final int rowAmount = cachedInventory.size() / columnAmount;
                            if (((columnAmount != 9) || ((rowAmount <= 6) && (rowAmount >= 1)))) {
                                player.getCapability(PlayerGUICapability.class).ifPresent(guiCapability -> {
                                    final IGuiInventory inventory = guiCapability.gui();
                                    IGuiInventoryUpdater updater = inventory.updater().title(plugin.actor(bukkitPlayer).getTranslatedMessageAsString(UIInventoryNames.LOOT_UI_NAME));
                                    if (columnAmount == 9) {
                                        updater.chestSize(CHEST_VALUES[rowAmount - 1]);
                                    } else {
                                        updater.type(cachedInventory.getType());
                                    }
                                    updater.apply();
                                    inventory.attrSet(LootUIHandler.ATTR_ID, cachedInventory.id());
                                    inventory.setHandler(LootUIHandler.LOOT_HANDLER);
                                    inventory.getInventory().setContents(cachedInventory.getItems());
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
                        actor.sendTranslatedBarMessage(Messages.CONTAINER_ACCESS_NOT_REPEATABLE);
                        return;
                    }
                    actor.sendTranslatedBarMessage(Messages.CONTAINER_ACCESS_NOT_ACCESSIBLE,
                        Key.of("time", DataHelper.formTimeString(actor, duration)));
                    return;
                }
                player.getCapability(PlayerGUICapability.class).ifPresent(guiCapability -> {
                    final IGuiInventory inventory = guiCapability.gui();
                    if (!(dataContainer instanceof final IInventoryContainer container)) {
                        // Do nothing, no need to allocate anything if we have no inventory
                        return;
                    }
                    final Inventory holderInventory = inventoryHolder.getInventory();
                    int columnAmount = IGuiInventory.getColumnAmount(holderInventory.getType());
                    IGuiInventoryUpdater updater = inventory.updater().title(plugin.actor(bukkitPlayer).getTranslatedMessageAsString(UIInventoryNames.LOOT_UI_NAME));
                    if (columnAmount == 9) {
                        updater.chestSize(CHEST_VALUES[(InventoryUtil.getSize(holderInventory) / columnAmount) - 1]);
                    } else {
                        updater.type(holderInventory.getType());
                    }
                    updater.apply();
                    inventory.attrSet(LootUIHandler.ATTR_ID, lookupTable.acquire(entryId));
                    inventory.setHandler(LootUIHandler.LOOT_HANDLER);
                    container.fill(player, inventoryHolder, location, inventory.getInventory());
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
