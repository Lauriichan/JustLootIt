package me.lauriichan.spigot.justlootit.command;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
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
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.storage.IStorage;

@Command(name = "debug", description = "A debug command")
public class DebugCommand {

    public static final Random RANDOM = new Random(System.currentTimeMillis());

    private static long newId(IStorage<?> storage) {
        long id = RANDOM.nextLong();
        while (storage.has(id) || !storage.isSupported(id)) {
            id = RANDOM.nextLong();
        }
        return id;
    }

    @Action("container frame")
    public void frameContainer(JustLootItPlugin plugin, Actor<?> actor) {
        Actor<Player> playerActor = actor.as(Player.class);
        if (!playerActor.isValid()) {
            actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ACTOR_NOT$SUPPORTED, Key.of("actorType", "Player"));
            return;
        }
        Player player = playerActor.getHandle();
        RayTraceResult result = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getEyeLocation().getDirection(), 5);
        if (result == null) {
            actor.sendMessage("&cYou have to look at an item frame!");
            return;
        }
        plugin.mainService().submit(() -> {
            Entity entity = result.getHitEntity();
            if(!(entity instanceof ItemFrame)) {
                actor.sendMessage("&cYou have to look at an item frame!");
                return;
            }
            ItemFrame itemFrame = (ItemFrame) entity;
            if(!itemFrame.getPersistentDataContainer().has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                actor.sendMessage("&cIs already a JustLootIt item frame");
                return;
            }
            ItemStack item = itemFrame.getItem();
            if(item == null || item.getType().isAir()) {
                actor.sendMessage("&cItem frame is empty.");
                return;
            }
            LevelAdapter level = plugin.versionHandler().getLevel(itemFrame.getWorld());
            level.getCapability(StorageCapability.class).ifPresentOrElse(capability -> {
                IStorage<Storable> storage = capability.storage();
                long id = newId(storage);
                itemFrame.getPersistentDataContainer().set(JustLootItKey.identity(), PersistentDataType.LONG, id);
                itemFrame.setItem(null);
                storage.write(new FrameContainer(id, item.clone()));
                actor.sendMessage("&aCreated item frame with id '" + Long.toHexString(id) + "'!");
            }, () -> {
                actor.sendMessage("&cNo storage available!");
            });
        });
    }

    @Action("container vanilla")
    public void vanillaContainer(JustLootItPlugin plugin, Actor<?> actor, @Argument(name = "loottable") String loottable,
        @Argument(name = "seed") long seed) {
        Actor<Player> playerActor = actor.as(Player.class);
        if (!playerActor.isValid()) {
            actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ACTOR_NOT$SUPPORTED, Key.of("actorType", "Player"));
            return;
        }
        NamespacedKey key = NamespacedKey.fromString(loottable);
        if (key == null) {
            actor.sendMessage("&cInvalid key '%s'!".formatted(loottable));
            return;
        }
        LootTable table = Bukkit.getLootTable(key);
        if (table == null) {
            actor.sendMessage("&cInvalid loottable '%s'!".formatted(key.toString()));
            return;
        }
        Player player = playerActor.getHandle();
        RayTraceResult result = player.getWorld().rayTraceBlocks(player.getEyeLocation(), player.getEyeLocation().getDirection(), 5);
        if (result == null) {
            actor.sendMessage("&cYou have to look at a block!");
            return;
        }
        plugin.mainService().submit(() -> {
            // If a exception happens in this lambda it will not be detected.
            Block block = result.getHitBlock();
            BlockState state = block.getState();
            if (!(state instanceof Container)) {
                actor.sendMessage("&cYou have to look at a container!");
                return;
            }
            Container stateContainer = (Container) state;
            if (stateContainer.getPersistentDataContainer().has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                actor.sendMessage("&cIs already a JustLootIt container");
                return;
            }
            LevelAdapter level = plugin.versionHandler().getLevel(block.getWorld());
            level.getCapability(StorageCapability.class).ifPresentOrElse(capability -> {
                IStorage<Storable> storage = capability.storage();
                long id = newId(storage);
                stateContainer.getPersistentDataContainer().set(JustLootItKey.identity(), PersistentDataType.LONG, id);
                stateContainer.getInventory().clear();
                stateContainer.update();
                storage.write(new VanillaContainer(id, table, seed));
                actor.sendMessage("&aCreated container with id '" + Long.toHexString(id) + "'!");
            }, () -> {
                actor.sendMessage("&cNo storage available!");
            });
        });
    }

    @Action("container static")
    public void vanillaContainer(JustLootItPlugin plugin, Actor<?> actor) {
        Actor<Player> playerActor = actor.as(Player.class);
        if (!playerActor.isValid()) {
            actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ACTOR_NOT$SUPPORTED, Key.of("actorType", "Player"));
            return;
        }
        Player player = playerActor.getHandle();
        RayTraceResult result = player.getWorld().rayTraceBlocks(player.getEyeLocation(), player.getEyeLocation().getDirection(), 5);
        if (result == null) {
            actor.sendMessage("&cYou have to look at a block!");
            return;
        }
        plugin.mainService().submit(() -> {
            // If a exception happens in this lambda it will not be detected.
            Block block = result.getHitBlock();
            BlockState state = block.getState();
            if (!(state instanceof Container)) {
                actor.sendMessage("&cYou have to look at a container!");
                return;
            }
            Container stateContainer = (Container) state;
            if (stateContainer.getPersistentDataContainer().has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                actor.sendMessage("&cIs already a JustLootIt container");
                return;
            }
            LevelAdapter level = plugin.versionHandler().getLevel(block.getWorld());
            level.getCapability(StorageCapability.class).ifPresentOrElse(capability -> {
                IStorage<Storable> storage = capability.storage();
                long id = newId(storage);
                stateContainer.getPersistentDataContainer().set(JustLootItKey.identity(), PersistentDataType.LONG, id);
                storage.write(new StaticContainer(id, stateContainer.getInventory()));
                stateContainer.getInventory().clear();
                stateContainer.update();
                actor.sendMessage("&aCreated container with id '" + Long.toHexString(id) + "'!");
            }, () -> {
                actor.sendMessage("&cNo storage available!");
            });
        });
    }

}