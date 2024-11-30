package me.lauriichan.spigot.justlootit.listener;

import java.time.Duration;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
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
import me.lauriichan.spigot.justlootit.data.FrameContainer;
import me.lauriichan.spigot.justlootit.message.Messages;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.model.IEntityData;
import me.lauriichan.spigot.justlootit.nms.model.IItemEntityData;
import me.lauriichan.spigot.justlootit.nms.packet.PacketOutSetEntityData;
import me.lauriichan.spigot.justlootit.nms.packet.PacketOutSetEntityData.IEntityDataPack;
import me.lauriichan.spigot.justlootit.nms.util.argument.ArgumentMap;
import me.lauriichan.spigot.justlootit.storage.Stored;
import me.lauriichan.spigot.justlootit.util.DataHelper;

@Extension
public class ItemFrameEventListener implements IListenerExtension {
    
    public static final String PLAYER_DATA_FRAME_LOOTING = "PlayerIsLootingFrame";
    
    private static final BlockData AIR_DATA = Bukkit.createBlockData(Material.AIR);

    private final JustLootItPlugin plugin;
    private final MainConfig config;

    public ItemFrameEventListener(final JustLootItPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.configManager().config(MainConfig.class);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractEvent(final PlayerInteractEntityEvent event) {
        final Entity entity = event.getRightClicked();
        EntityType type;
        if (entity == null || (type = entity.getType()) != EntityType.ITEM_FRAME && type != EntityType.GLOW_ITEM_FRAME) {
            return;
        }
        final PersistentDataContainer container = entity.getPersistentDataContainer();
        if (!JustLootItAccess.hasIdentity(container)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingBreak(final HangingBreakEvent event) {
        final Entity entity = event.getEntity();
        EntityType type;
        if (entity == null || (type = entity.getType()) != EntityType.ITEM_FRAME && type != EntityType.GLOW_ITEM_FRAME) {
            return;
        }
        event.setCancelled(JustLootItAccess.hasIdentity(entity.getPersistentDataContainer()));
        if (event.getCause() == RemoveCause.OBSTRUCTION) {
            entity.getWorld().setBlockData(event.getEntity().getLocation(), AIR_DATA);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingBreakByEntity(final HangingBreakByEntityEvent event) {
        final Entity entity = event.getEntity();
        EntityType type;
        if (entity == null || (type = entity.getType()) != EntityType.ITEM_FRAME && type != EntityType.GLOW_ITEM_FRAME) {
            return;
        }
        final PersistentDataContainer container = entity.getPersistentDataContainer();
        if (!JustLootItAccess.hasIdentity(container)) {
            return;
        }
        event.setCancelled(true);
        final Entity remover = event.getRemover();
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
            actor.versionHandler().getLevel(entity.getWorld()).getCapability(StorageCapability.class).ifPresent(capability -> {
                final Stored<FrameContainer> stored = capability.storage().read(id);
                final FrameContainer frame = stored.value();
                if (!frame.access(player.getUniqueId())) {
                    final Duration duration = frame.durationUntilNextAccess(player.getUniqueId());
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
                    final IEntityData data = pack.getById(8);
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
