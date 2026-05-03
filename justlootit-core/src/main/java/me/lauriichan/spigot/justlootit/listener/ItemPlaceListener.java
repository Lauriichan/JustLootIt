package me.lauriichan.spigot.justlootit.listener;

import java.util.Random;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Directional;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.RayTraceResult;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.listener.IListenerExtension;
import me.lauriichan.spigot.justlootit.JustLootItAccess;
import me.lauriichan.spigot.justlootit.JustLootItKey;
import me.lauriichan.spigot.justlootit.JustLootItPermission;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.compatibility.data.CompatibilityDataExtension;
import me.lauriichan.spigot.justlootit.data.CompatibilityContainer;
import me.lauriichan.spigot.justlootit.data.Container;
import me.lauriichan.spigot.justlootit.data.CustomContainer;
import me.lauriichan.spigot.justlootit.data.VanillaContainer;
import me.lauriichan.spigot.justlootit.message.Messages;
import me.lauriichan.spigot.justlootit.storage.IStorage;
import me.lauriichan.spigot.justlootit.storage.Stored;
import me.lauriichan.spigot.justlootit.util.BlockUtil;
import me.lauriichan.spigot.justlootit.util.EntityUtil;
import me.lauriichan.spigot.justlootit.util.persistence.TableKey;

@Extension
public class ItemPlaceListener implements IListenerExtension {

    private final JustLootItPlugin plugin;
    private final Random seedRandom = new Random(System.currentTimeMillis());

    public ItemPlaceListener(final JustLootItPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityPlace(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack itemStack = event.getItem();
        if (!itemStack.hasItemMeta()) {
            return;
        }
        PersistentDataContainer itemContainer = itemStack.getItemMeta().getPersistentDataContainer();
        if (!itemContainer.has(JustLootItKey.identity(), TableKey.KEY_TYPE)) {
            return;
        }
        EntityType type;
        try {
            type = EntityType.valueOf(itemStack.getType().name());
        } catch (IllegalArgumentException iae) {
            return;
        }
        if (!EntityUtil.isSupportedEntity(type) && !EntityUtil.isItemFrame(type)) {
            return;
        }
        event.setCancelled(true);
        Actor<Player> actor = plugin.actor(event.getPlayer());
        if (!actor.hasPermission(JustLootItPermission.ACTION_PLACE_CONTAINER_ENTITY)) {
            actor.sendTranslatedMessage(Messages.CONTAINER_PLACE_UNPERMITTED_ENTITY);
            return;
        }
        TableKey key = itemContainer.get(JustLootItKey.identity(), TableKey.KEY_TYPE);
        Block targetBlock = event.getClickedBlock();
        BlockFace targetFace = event.getBlockFace();
        Material blockType = targetBlock.getType();
        if (EntityUtil.isChestBoat(type)) {
            if (blockType != Material.WATER) {
                RayTraceResult result = event.getPlayer().rayTraceBlocks(5, FluidCollisionMode.ALWAYS);
                if (result != null) {
                    Block hit = result.getHitBlock();
                    if (hit != null && hit.getType() == Material.WATER) {
                        targetBlock = hit;
                        targetFace = result.getHitBlockFace();
                    }
                }
            }
        } else if (EntityUtil.isMinecart(type)) {
            if (blockType != Material.RAIL && !blockType.name().endsWith("_RAIL")) {
                return;
            }
        }
        int inventorySize = EntityUtil.getInventorySize(type);
        if (inventorySize == 0) {
            // This is an item frame
            actor.sendTranslatedMessage(Messages.CONTAINER_PLACE_FAILURE_ENTITY);
            return;
        }
        Entity entity = targetBlock.getWorld().spawnEntity(targetBlock.getLocation().add(0.5, 0, 0.5), type);
        if (entity instanceof Directional directional) {
            directional.setFacingDirection(targetFace.getOppositeFace());
        }
        entity.setPersistent(true);
        plugin.versionHandler().getLevel(entity.getWorld()).getCapability(StorageCapability.class).ifPresentOrElse(capability -> {
            IStorage storage = capability.storage();
            PersistentDataContainer data = entity.getPersistentDataContainer();
            Container jliContainer = null;
            long seed = seedRandom.nextLong();
            switch (key.type()) {
            case COMPATIBILITY:
                jliContainer = new CompatibilityContainer(CompatibilityDataExtension.get(key.namespace()).createData(key.key(), seed));
                break;
            case CUSTOM:
                jliContainer = new CustomContainer(NamespacedKey.fromString(key.namespace() + ':' + key.key()), seed);
                break;
            case VANILLA:
                jliContainer = new VanillaContainer(NamespacedKey.fromString(key.namespace() + ':' + key.key()), seed);
                break;
            }
            if (jliContainer == null) {
                event.setCancelled(true);
                actor.sendTranslatedMessage(Messages.CONTAINER_PLACE_FAILURE_ENTITY);
                return;
            }
            Stored<?> stored;
            storage.write(stored = storage.registry().create(jliContainer));
            JustLootItAccess.setIdentity(data, stored.id());
            actor.sendTranslatedMessage(Messages.CONTAINER_PLACE_CREATED_ENTITY, Key.of("id", stored.id()), Key.of("seed", seed));
        }, () -> {
            event.setCancelled(true);
            actor.sendTranslatedMessage(Messages.CONTAINER_PLACE_FAILURE_ENTITY);
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack itemStack = event.getItemInHand();
        if (!itemStack.hasItemMeta()) {
            return;
        }
        PersistentDataContainer itemContainer = itemStack.getItemMeta().getPersistentDataContainer();
        if (!itemContainer.has(JustLootItKey.identity(), TableKey.KEY_TYPE)) {
            return;
        }
        Actor<Player> actor = plugin.actor(event.getPlayer());
        if (!actor.hasPermission(JustLootItPermission.ACTION_PLACE_CONTAINER_BLOCK)) {
            event.setCancelled(true);
            actor.sendTranslatedMessage(Messages.CONTAINER_PLACE_UNPERMITTED_BLOCK);
            return;
        }
        TableKey key = itemContainer.get(JustLootItKey.identity(), TableKey.KEY_TYPE);
        Block block = event.getBlock();
        if (!(block.getState() instanceof org.bukkit.block.Container container)) {
            event.setCancelled(true);
            actor.sendTranslatedMessage(Messages.CONTAINER_PLACE_FAILURE_BLOCK);
            return;
        }
        plugin.versionHandler().getLevel(container.getWorld()).getCapability(StorageCapability.class).ifPresentOrElse(capability -> {
            IStorage storage = capability.storage();
            PersistentDataContainer containerData = container.getPersistentDataContainer();
            Container jliContainer = null;
            long seed = seedRandom.nextLong();
            switch (key.type()) {
            case COMPATIBILITY:
                jliContainer = new CompatibilityContainer(CompatibilityDataExtension.get(key.namespace()).createData(key.key(), seed));
                break;
            case CUSTOM:
                jliContainer = new CustomContainer(NamespacedKey.fromString(key.namespace() + ':' + key.key()), seed);
                break;
            case VANILLA:
                jliContainer = new VanillaContainer(NamespacedKey.fromString(key.namespace() + ':' + key.key()), seed);
                break;
            }
            if (jliContainer == null) {
                event.setCancelled(true);
                actor.sendTranslatedMessage(Messages.CONTAINER_PLACE_FAILURE_BLOCK);
                return;
            }
            if (container.getBlockData() instanceof Chest chest && chest.getType() != Type.SINGLE) {
                org.bukkit.block.Container otherContainer = BlockUtil.findChestAround(container.getWorld(), container.getLocation(),
                    chest.getType(), chest.getFacing());
                if (otherContainer != null) {
                    PersistentDataContainer otherContainerData = otherContainer.getPersistentDataContainer();
                    boolean applyUpdate = true;
                    if (JustLootItAccess.hasIdentity(otherContainerData)) {
                        long id = JustLootItAccess.getIdentity(otherContainerData);
                        final Stored<Container> dataContainer = storage.read(id);
                        if (applyUpdate = (dataContainer == null)) {
                            JustLootItAccess.removeIdentity(otherContainerData);
                        }
                    }
                    if (applyUpdate) {
                        Chest cloned = (Chest) chest.clone();
                        cloned.setType(cloned.getType() == Type.LEFT ? Type.RIGHT : Type.LEFT);
                        otherContainer.setBlockData(cloned);
                        BlockUtil.setContainerOffset(container, otherContainer, false);
                        otherContainer.update();
                    }
                }
            }
            Stored<?> stored;
            storage.write(stored = storage.registry().create(jliContainer));
            JustLootItAccess.setIdentity(containerData, stored.id());
            container.update();
            actor.sendTranslatedMessage(Messages.CONTAINER_PLACE_CREATED_BLOCK, Key.of("id", stored.id()), Key.of("seed", seed));
        }, () -> {
            event.setCancelled(true);
            actor.sendTranslatedMessage(Messages.CONTAINER_PLACE_FAILURE_BLOCK);
        });
    }

}