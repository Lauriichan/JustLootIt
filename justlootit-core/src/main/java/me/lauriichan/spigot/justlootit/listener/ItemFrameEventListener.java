package me.lauriichan.spigot.justlootit.listener;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.Skull;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;

import me.lauriichan.laylib.localization.Key;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.listener.IListenerExtension;
import me.lauriichan.spigot.justlootit.JustLootItAccess;
import me.lauriichan.spigot.justlootit.JustLootItPermission;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.capability.ActorCapability;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.command.impl.LootItActor;
import me.lauriichan.spigot.justlootit.config.MainConfig;
import me.lauriichan.spigot.justlootit.config.world.WorldConfig;
import me.lauriichan.spigot.justlootit.config.world.WorldMultiConfig;
import me.lauriichan.spigot.justlootit.data.FrameContainer;
import me.lauriichan.spigot.justlootit.message.Messages;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.model.IEntityData;
import me.lauriichan.spigot.justlootit.nms.model.IItemEntityData;
import me.lauriichan.spigot.justlootit.nms.packet.PacketOutSetEntityData;
import me.lauriichan.spigot.justlootit.nms.packet.PacketOutSetEntityData.IEntityDataPack;
import me.lauriichan.spigot.justlootit.nms.util.argument.ArgumentMap;
import me.lauriichan.spigot.justlootit.storage.Stored;
import me.lauriichan.spigot.justlootit.util.DataHelper;
import me.lauriichan.spigot.justlootit.util.EntityUtil;
import me.lauriichan.spigot.justlootit.util.ExplosionType;

@Extension
public class ItemFrameEventListener implements IListenerExtension {

    public static final String PLAYER_DATA_FRAME_LOOTING = "PlayerIsLootingFrame";

    private final JustLootItPlugin plugin;
    private final VersionHandler versionHandler;
    private final MainConfig config;

    public ItemFrameEventListener(final JustLootItPlugin plugin) {
        this.plugin = plugin;
        this.versionHandler = plugin.versionHandler();
        this.config = plugin.configManager().config(MainConfig.class);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractEvent(final PlayerInteractEntityEvent event) {
        final Entity entity = event.getRightClicked();
        if (entity == null || !EntityUtil.isItemFrame(entity.getType())) {
            return;
        }
        final PersistentDataContainer container = entity.getPersistentDataContainer();
        if (!JustLootItAccess.hasIdentity(container)) {
            return;
        }
        event.setCancelled(true);
        if (!VersionHandler.isPaper()) {
            return;
        }
        // Paper for some reason decides to send a entity update here,
        // since the ItemFrame is being cleared by JLI on container creation it will
        // send the player an empty ItemFrame as a result.
        // Therefore a packet needs to be sent in order to fix this issue.
        // So just for Paper we have to read the container into memory and send a fix...
        final long id = JustLootItAccess.getIdentity(container);
        final World world = entity.getWorld();
        versionHandler.getLevel(world).getCapability(StorageCapability.class).ifPresent(capability -> {
            final Stored<FrameContainer> stored = capability.storage().read(id);
            if (!stored.value().canAccess(world, event.getPlayer().getUniqueId())) {
                // If the player can not access this item frame we don't need to send an update
                return;
            }
            final PacketOutSetEntityData dataPacket = versionHandler.packetManager().createPacket(new ArgumentMap().set("entity", entity),
                PacketOutSetEntityData.class);
            final IEntityDataPack pack = dataPacket.getData();
            final IEntityData data = pack.getById(versionHandler.versionHelper().getItemFrameItemDataId());
            if (!(data instanceof IItemEntityData itemData)) {
                return;
            }
            itemData.setItem(stored.value().getItem());
            final PlayerAdapter player = versionHandler.getPlayer(event.getPlayer());
            versionHandler.platform().scheduler().sync(() -> player.send(dataPacket));
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingBreak(final HangingBreakEvent event) {
        final Entity entity = event.getEntity();
        if (entity == null || !EntityUtil.isItemFrame(entity.getType())
            || !JustLootItAccess.hasIdentity(entity.getPersistentDataContainer())) {
            return;
        }
        event.setCancelled(true);
        if (event.getCause() != RemoveCause.OBSTRUCTION) {
            if (event.getCause() == RemoveCause.EXPLOSION) {
                event.setCancelled(handleExplosion(entity, null, entity.getLastDamageCause()));
                return;
            }
            return;
        }
        BlockState state = entity.getWorld().getBlockState(event.getEntity().getLocation());
        if (state.getType().isAir()) {
            return;
        }
        List<ItemStack> drops = new ArrayList<>();
        ItemStack stack = new ItemStack(state.getType());
        drops.add(stack);
        if (state instanceof TileState) {
            ItemMeta meta = stack.getItemMeta();
            if (meta instanceof BlockStateMeta blockMeta) {
                if (state instanceof InventoryHolder holder && !(holder instanceof ShulkerBox)) {
                    holder.getInventory().getViewers().forEach(HumanEntity::closeInventory);
                    for (ItemStack content : holder.getInventory().getContents()) {
                        if (content == null || content.getType().isAir()) {
                            continue;
                        }
                        drops.add(content.clone());
                    }
                    holder.getInventory().clear();
                } else {
                    blockMeta.setBlockState(state.copy());
                }
            } else if (meta instanceof SkullMeta skullMeta) {
                skullMeta.setOwnerProfile(((Skull) state).getOwnerProfile());
            } else if (meta instanceof BannerMeta bannerMeta) {
                bannerMeta.setPatterns(((Banner) state).getPatterns());
            }
            stack.setItemMeta(meta);
        }
        World world = entity.getWorld();
        Location location = entity.getLocation();
        state.setType(Material.AIR);
        state.update(true, false);
        for (ItemStack drop : drops) {
            world.dropItem(location, drop);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingBreakByEntity(final HangingBreakByEntityEvent event) {
        final Entity entity = event.getEntity();
        EntityType type;
        if (entity == null || !EntityUtil.isItemFrame(type = entity.getType())) {
            return;
        }
        final PersistentDataContainer container = entity.getPersistentDataContainer();
        if (!JustLootItAccess.hasIdentity(container)) {
            return;
        }
        event.setCancelled(true);
        final Entity remover = event.getRemover();
        if (event.getCause() == RemoveCause.EXPLOSION) {
            event.setCancelled(handleExplosion(entity, remover, entity.getLastDamageCause()));
            return;
        }
        if (remover.getType() != EntityType.PLAYER) {
            return;
        }
        final Player player = (Player) remover;
        if (!player.isSneaking()) {
            accessFrame(entity, type, player, container);
            return;
        }
        LootItActor<Player> actor = ActorCapability.actor(plugin, player);
        if (!player.hasPermission(JustLootItPermission.ACTION_REMOVE_CONTAINER_ENTITY)) {
            actor.sendTranslatedMessage(Messages.CONTAINER_BREAK_UNPERMITTED_ENTITY);
            return;
        }
        breakFrame(entity, actor, container);
    }

    private boolean handleExplosion(Entity entity, Entity remover, EntityDamageEvent lastCause) {
        ExplosionType type;
        if (lastCause instanceof EntityDamageByBlockEvent dmgBlock) {
            type = ExplosionType.fromBlock(dmgBlock.getDamager().getBlockData().getMaterial());
        } else if (lastCause instanceof EntityDamageByEntityEvent dmgEntity) {
            type = ExplosionType.fromEntity(dmgEntity.getEntityType());
        } else {
            // We add a special type cause it seems like the last damage cause is never given.
            // Instead it is always just destroyed with the Removal cause of EXPLOSION which sadly makes this
            // basically undetectable other than saying yeah it is player caused.
            type = remover == null ? ExplosionType.UNKNOWN : ExplosionType.fromEntity(remover.getType());
        }
        WorldConfig worldConfig = plugin.configManager().multiConfig(WorldMultiConfig.class, entity.getWorld());
        if (!worldConfig.isExplosionAllowed(type)) {
            return true;
        }
        PersistentDataContainer container = entity.getPersistentDataContainer();
        final long id = JustLootItAccess.getIdentity(container);
        JustLootItAccess.removeIdentity(container);
        versionHandler
            .broadcast(versionHandler.packetManager().createPacket(new ArgumentMap().set("entity", entity), PacketOutSetEntityData.class));
        if (config.deleteOnBreak()) {
            versionHandler.getLevel(entity.getWorld()).getCapability(StorageCapability.class)
                .ifPresent(capability -> capability.storage().delete(id));
        }
        return false;
    }

    private void breakFrame(Entity entity, LootItActor<Player> actor, PersistentDataContainer container) {
        if (!DataHelper.canBreakContainer(container, actor.getId())) {
            actor.sendTranslatedMessage(Messages.CONTAINER_BREAK_CONFIRMATION_ENTITY);
            return;
        }
        final long id = JustLootItAccess.getIdentity(container);
        JustLootItAccess.removeIdentity(container);
        actor.sendTranslatedMessage(Messages.CONTAINER_BREAK_REMOVED_ENTITY, Key.of("id", id));
        actor.versionHandler().broadcast(
            actor.versionHandler().packetManager().createPacket(new ArgumentMap().set("entity", entity), PacketOutSetEntityData.class));
        if (!config.deleteOnBreak()) {
            return;
        }
        actor.versionHandler().getLevel(entity.getWorld()).getCapability(StorageCapability.class).ifPresent(capability -> {
            if (!capability.storage().delete(id)) {
                actor.sendTranslatedMessage(Messages.CONTAINER_BREAK_NO_CONTAINER, Key.of("id", id));
            }
        });
    }

    private void accessFrame(Entity entity, EntityType type, Player player, PersistentDataContainer container) {
        final LootItActor<?> actor = ActorCapability.actor(plugin, player);
        PlayerAdapter adapter = actor.versionHandler().getPlayer(player);
        if (adapter.getDataOrFallback(PLAYER_DATA_FRAME_LOOTING, false, boolean.class)) {
            return;
        }
        adapter.setData(PLAYER_DATA_FRAME_LOOTING, true);
        try {
            final long id = JustLootItAccess.getIdentity(container);
            final World world = entity.getWorld();
            actor.versionHandler().getLevel(world).getCapability(StorageCapability.class).ifPresent(capability -> {
                final Stored<FrameContainer> stored = capability.storage().read(id);
                final FrameContainer frame = stored.value();
                if (!frame.access(world, player.getUniqueId())) {
                    final Duration duration = frame.durationUntilNextAccess(world, player.getUniqueId());
                    if (duration.isNegative()) {
                        actor.sendTranslatedBarMessage(Messages.CONTAINER_ACCESS_NOT_REPEATABLE);
                        return;
                    }
                    actor.sendTranslatedBarMessage(Messages.CONTAINER_ACCESS_NOT_ACCESSIBLE,
                        Key.of("time", DataHelper.formTimeString(actor, duration)));
                    return;
                }
                final ItemStack itemStack = frame.getItem().clone();
                if (!player.getInventory().addItem(itemStack).isEmpty()) {
                    player.getWorld().dropItemNaturally(entity.getLocation(), itemStack);
                }
                player.playSound(entity.getLocation(),
                    type == EntityType.GLOW_ITEM_FRAME ? Sound.ENTITY_GLOW_ITEM_FRAME_REMOVE_ITEM : Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM,
                    SoundCategory.NEUTRAL, 1f, 1f);
                final PacketOutSetEntityData packet = actor.versionHandler().packetManager()
                    .createPacket(new ArgumentMap().set("entity", entity), PacketOutSetEntityData.class);
                if (packet != null) {
                    final IEntityDataPack pack = packet.getData();
                    final IEntityData data = pack.getById(actor.versionHelper().getItemFrameItemDataId());
                    if (data instanceof final IItemEntityData itemData) {
                        itemData.setItem(null);
                        adapter.send(packet);
                    }
                }
            });
        } finally {
            adapter.removeData(PLAYER_DATA_FRAME_LOOTING);
        }
    }

}
