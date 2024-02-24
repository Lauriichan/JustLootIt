package me.lauriichan.spigot.justlootit.command;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.annotation.Action;
import me.lauriichan.laylib.command.annotation.Argument;
import me.lauriichan.laylib.command.annotation.Command;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.spigot.justlootit.JustLootItKey;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.data.FrameContainer;
import me.lauriichan.spigot.justlootit.data.StaticContainer;
import me.lauriichan.spigot.justlootit.data.VanillaContainer;
import me.lauriichan.spigot.justlootit.message.Messages;
import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import me.lauriichan.spigot.justlootit.storage.IStorage;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.util.BlockUtil;
import me.lauriichan.spigot.justlootit.util.SimpleDataType;

@Command(name = "debug", description = "A debug command")
public class DebugCommand {

    @Action("a")
    public void a() {}
    @Action("b")
    public void b() {}
    @Action("c")
    public void c() {}
    @Action("d")
    public void d() {}
    
    @Action("config reload")
    public void configReload(final JustLootItPlugin plugin, final Actor<?> actor) {
        actor.sendMessage("Reloading config...");
        plugin.configManager().reload();
        actor.sendMessage("Config reloaded.");
    }

    @Action("info pdc")
    public void infoPdc(final JustLootItPlugin plugin, final Actor<?> actor) {
        final Actor<Player> playerActor = actor.as(Player.class);
        if (!playerActor.isValid()) {
            actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ACTOR_NOT$SUPPORTED, Key.of("actorType", "Player"));
            return;
        }
        final Player player = playerActor.getHandle();
        final RayTraceResult result = player.getWorld().rayTraceBlocks(player.getEyeLocation(), player.getLocation().getDirection(), 5);
        if (result == null) {
            actor.sendMessage("&cYou have to look at a block!");
            return;
        }
        plugin.mainService().submit(() -> {
            // If a exception happens in this lambda it will not be detected.
            final Block block = result.getHitBlock();
            final BlockState state = block.getState();
            if (!(state instanceof PersistentDataHolder holder)) {
                actor.sendMessage("&cYou have to look at a persistent data holding block!");
                return;
            }
            String data = plugin.versionHandler().debugHelper().persistentDataAsString(holder.getPersistentDataContainer());
            if (data.isEmpty()) {
                actor.sendMessage("&eBlock has no persistent data set");
                return;
            }
            actor.sendMessage("&a" + data.replace("\r", ""));
        });
    }

    @Action("container frame")
    public void frameContainer(final JustLootItPlugin plugin, final Actor<?> actor) {
        final Actor<Player> playerActor = actor.as(Player.class);
        if (!playerActor.isValid()) {
            actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ACTOR_NOT$SUPPORTED, Key.of("actorType", "Player"));
            return;
        }
        final Player player = playerActor.getHandle();
        final RayTraceResult result = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getLocation().getDirection(), 5);
        if (result == null) {
            actor.sendMessage("&cYou have to look at an item frame!");
            return;
        }
        plugin.mainService().submit(() -> {
            final Entity entity = result.getHitEntity();
            if (!(entity instanceof ItemFrame)) {
                actor.sendMessage("&cYou have to look at an item frame!");
                return;
            }
            final ItemFrame itemFrame = (ItemFrame) entity;
            if (itemFrame.getPersistentDataContainer().has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                actor.sendMessage("&cIs already a JustLootIt item frame");
                return;
            }
            final ItemStack item = itemFrame.getItem();
            if (item == null || item.getType().isAir()) {
                actor.sendMessage("&cItem frame is empty.");
                return;
            }
            final LevelAdapter level = plugin.versionHandler().getLevel(itemFrame.getWorld());
            level.getCapability(StorageCapability.class).ifPresentOrElse(capability -> {
                final IStorage<Storable> storage = capability.storage();
                final long id = storage.newId();
                itemFrame.getPersistentDataContainer().set(JustLootItKey.identity(), PersistentDataType.LONG, id);
                storage.write(new FrameContainer(id, item.clone()));
                itemFrame.setItem(null);
                actor.sendMessage("&aCreated item frame with id '" + Long.toHexString(id) + "'!");
            }, () -> {
                actor.sendMessage("&cNo storage available!");
            });
        });
    }

    @Action("container vanilla")
    public void vanillaContainer(final JustLootItPlugin plugin, final Actor<?> actor, @Argument(name = "loottable") final LootTable loottable,
        @Argument(name = "seed") final long seed) {
        final Actor<Player> playerActor = actor.as(Player.class);
        if (!playerActor.isValid()) {
            actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ACTOR_NOT$SUPPORTED, Key.of("actorType", "Player"));
            return;
        }
        final Player player = playerActor.getHandle();
        final RayTraceResult result = player.getWorld().rayTraceBlocks(player.getEyeLocation(), player.getLocation().getDirection(), 5);
        if (result == null) {
            actor.sendMessage("&cYou have to look at a block!");
            return;
        }
        plugin.mainService().submit(() -> {
            // If a exception happens in this lambda it will not be detected.
            final Block block = result.getHitBlock();
            final BlockState state = block.getState();
            if (!(state instanceof Container)) {
                actor.sendMessage("&cYou have to look at a container!");
                return;
            }
            final Container stateContainer = (Container) state;
            final PersistentDataContainer stateDataContainer = stateContainer.getPersistentDataContainer();
            if (stateDataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)
                || stateDataContainer.has(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR)) {
                actor.sendMessage("&cIs already a JustLootIt container");
                return;
            }
            final LevelAdapter level = plugin.versionHandler().getLevel(block.getWorld());
            level.getCapability(StorageCapability.class).ifPresentOrElse(capability -> {
                final IStorage<Storable> storage = capability.storage();
                final long id = storage.newId();
                stateDataContainer.set(JustLootItKey.identity(), PersistentDataType.LONG, id);
                final BlockData data = state.getBlockData();
                if (data instanceof Chest chest && chest.getType() != Type.SINGLE) {
                    final Container otherContainer = BlockUtil.findChestAround(block.getWorld(), state.getLocation(), chest.getType(), chest.getFacing());
                    if (otherContainer != null) {
                        stateDataContainer.set(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR,
                            otherContainer.getLocation().toVector().subtract(stateContainer.getLocation().toVector()));
                        otherContainer.getPersistentDataContainer().set(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR,
                            stateContainer.getLocation().toVector().subtract(otherContainer.getLocation().toVector()));
                        otherContainer.update();
                    }
                }
                stateContainer.update();
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Inventory inv = ((Container) stateContainer.getBlock().getState()).getInventory();
                    if(inv.getHolder() instanceof DoubleChest chest) {
                        chest.getLeftSide().getInventory().clear();
                        chest.getRightSide().getInventory().clear();
                    } else {
                        inv.clear();
                    }
                }, 1);
                storage.write(new VanillaContainer(id, loottable, seed));
                actor.sendMessage("&aCreated container with id '" + Long.toHexString(id) + "'!");
            }, () -> {
                actor.sendMessage("&cNo storage available!");
            });
        });
    }

    @Action("container static")
    public void vanillaContainer(final JustLootItPlugin plugin, final Actor<?> actor) {
        final Actor<Player> playerActor = actor.as(Player.class);
        if (!playerActor.isValid()) {
            actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ACTOR_NOT$SUPPORTED, Key.of("actorType", "Player"));
            return;
        }
        final Player player = playerActor.getHandle();
        final RayTraceResult result = player.getWorld().rayTraceBlocks(player.getEyeLocation(), player.getLocation().getDirection(), 5);
        if (result == null) {
            actor.sendMessage("&cYou have to look at a block!");
            return;
        }
        plugin.mainService().submit(() -> {
            // If a exception happens in this lambda it will not be detected.
            final Block block = result.getHitBlock();
            final BlockState state = block.getState();
            if (!(state instanceof Container)) {
                actor.sendMessage("&cYou have to look at a container!");
                return;
            }
            final Container stateContainer = (Container) state;
            final PersistentDataContainer stateDataContainer = stateContainer.getPersistentDataContainer();
            if (stateDataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)
                || stateDataContainer.has(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR)) {
                actor.sendMessage("&cIs already a JustLootIt container");
                return;
            }
            final LevelAdapter level = plugin.versionHandler().getLevel(block.getWorld());
            level.getCapability(StorageCapability.class).ifPresentOrElse(capability -> {
                final IStorage<Storable> storage = capability.storage();
                final long id = storage.newId();
                stateDataContainer.set(JustLootItKey.identity(), PersistentDataType.LONG, id);
                final BlockData data = state.getBlockData();
                if (data instanceof Chest chest && chest.getType() != Type.SINGLE) {
                    final Container otherContainer = BlockUtil.findChestAround(block.getWorld(), state.getLocation(), chest.getType(), chest.getFacing());
                    if (otherContainer != null) {
                        stateDataContainer.set(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR,
                            otherContainer.getLocation().toVector().subtract(stateContainer.getLocation().toVector()));
                        otherContainer.getPersistentDataContainer().set(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR,
                            stateContainer.getLocation().toVector().subtract(otherContainer.getLocation().toVector()));
                        otherContainer.update();
                    }
                }
                final Inventory inventory = stateContainer.getInventory();
                storage.write(new StaticContainer(id, inventory));
                stateContainer.update();
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Inventory inv = ((Container) stateContainer.getBlock().getState()).getInventory();
                    if(inv.getHolder() instanceof DoubleChest chest) {
                        chest.getLeftSide().getInventory().clear();
                        chest.getRightSide().getInventory().clear();
                    } else {
                        inv.clear();
                    }
                }, 1);
                actor.sendMessage("&aCreated container with id '" + Long.toHexString(id) + "'!");
            }, () -> {
                actor.sendMessage("&cNo storage available!");
            });
        });
    }

}
