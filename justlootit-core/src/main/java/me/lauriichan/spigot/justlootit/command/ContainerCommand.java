package me.lauriichan.spigot.justlootit.command;

import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.annotation.Action;
import me.lauriichan.laylib.command.annotation.Argument;
import me.lauriichan.laylib.command.annotation.Command;
import me.lauriichan.laylib.command.annotation.Param;
import me.lauriichan.laylib.command.annotation.Permission;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.minecraft.pluginbase.message.component.Component;
import me.lauriichan.spigot.justlootit.JustLootItKey;
import me.lauriichan.spigot.justlootit.JustLootItPermission;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.command.argument.CoordinateArgument.Coord;
import me.lauriichan.spigot.justlootit.data.Container;
import me.lauriichan.spigot.justlootit.data.FrameContainer;
import me.lauriichan.spigot.justlootit.data.VanillaContainer;
import me.lauriichan.spigot.justlootit.message.Messages;
import me.lauriichan.spigot.justlootit.util.SimpleDataType;
import me.lauriichan.spigot.justlootit.util.TypeName;
import net.md_5.bungee.api.chat.HoverEvent;

@Extension
@Command(name = "container")
@Permission(JustLootItPermission.COMMAND_CONTAINER)
public class ContainerCommand implements ICommandExtension {

    @Action("info")
    public void info(final JustLootItPlugin plugin, final Actor<?> actor,
        @Argument(name = "x", index = 0, params = @Param(name = "axis", stringValue = "x", type = Param.TYPE_STRING)) final Coord x,
        @Argument(name = "y", index = 1, params = @Param(name = "axis", stringValue = "y", type = Param.TYPE_STRING)) final Coord y,
        @Argument(name = "z", index = 2, params = @Param(name = "axis", stringValue = "z", type = Param.TYPE_STRING)) final Coord z,
        @Argument(name = "world", optional = true, index = 3) World world) {
        if (world == null) {
            if (!(actor.getHandle() instanceof Entity entity)) {
                actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ACTOR_WORLD_REQUIRED);
                return;
            }
            world = entity.getWorld();
        }
        final World finalWorld = world;
        plugin.mainService().submit(() -> {
            Block block = finalWorld.getBlockAt(x.value(), y.value(), z.value());
            if (block.isEmpty()) {
                doEntityContainer(plugin, finalWorld, actor, x.value(), y.value(), z.value());
                return;
            }
            BlockState state = block.getState();
            if (!(state instanceof org.bukkit.block.Container stateContainer)) {
                doEntityContainer(plugin, finalWorld, actor, x.value(), y.value(), z.value());
                return;
            }
            doBlockContainer(plugin, block, stateContainer, finalWorld, actor, x.value(), y.value(), z.value());
        });
    }

    private void doEntityContainer(JustLootItPlugin plugin, World world, Actor<?> actor, int x, int y, int z) {
        Collection<Entity> entities = world
            .getNearbyEntities(new Location(world, x + 0.5d, y + 0.5d, z + 0.5d), 1.5d, 1.5d, 1.5d);
        if (entities.isEmpty()) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_INFO_NO_CONTAINER_BLOCK, Key.of("x", x),
                Key.of("y", y), Key.of("z", z), Key.of("world", world.getName()));
            return;
        }
        List<ItemFrame> validEntities = entities.stream().filter(entity -> entity instanceof ItemFrame).map(entity -> (ItemFrame) entity).toList();
        if (validEntities.isEmpty()) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_INFO_NO_CONTAINER_BLOCK, Key.of("x", x), Key.of("y", y), Key.of("z", z),
                Key.of("world", world.getName()));
            return;
        }
        double distance = Double.MAX_VALUE;
        Location origin = new Location(world, x + 0.5d, y + 0.5d, z + 0.5d);
        Entity closest = null;
        for (Entity entity : validEntities) {
            Location current = entity.getLocation();
            double dist = origin.distanceSquared(current);
            if (dist < distance) {
                closest = entity;
                distance = dist;
            }
        }
        Location loc = closest.getLocation();
        PersistentDataContainer dataContainer = closest.getPersistentDataContainer();
        if (!dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_INFO_NO_CONTAINER_ENTITY, Key.of("x", loc.getX()),
                Key.of("y", loc.getY()), Key.of("z", loc.getZ()), Key.of("world", world.getName()));
            return;
        }
        long id = dataContainer.get(JustLootItKey.identity(), PersistentDataType.LONG);
        plugin.versionHandler().getLevel(world).getCapability(StorageCapability.class).ifPresentOrElse(capability -> {
            Container container = (Container) capability.storage().read(id);
            if (container == null) {
                dataContainer.remove(JustLootItKey.identity());
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_INFO_NO_CONTAINER_ENTITY, Key.of("x", loc.getX()),
                    Key.of("y", loc.getY()), Key.of("z", loc.getZ()), Key.of("world", world.getName()));
                return;
            }
            String refreshGroup = container.getGroupId();
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_INFO_CONTAINER_ANY_ENTITY, Key.of("id", container.id()),
                Key.of("refreshGroup", refreshGroup == null || refreshGroup.isEmpty() ? "None" : refreshGroup), Key.of("type", TypeName.ofContainer(container)),
                Key.of("x", loc.getX()), Key.of("y", loc.getY()), Key.of("z", loc.getZ()), Key.of("world", world.getName()));
            if (container instanceof VanillaContainer vanilla) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_INFO_CONTAINER_VANILLA, Key.of("seed", vanilla.getSeed()),
                    Key.of("lootTable", vanilla.getLootTableKey()));
            } else if (container instanceof FrameContainer frame) {
                Component component = Component.of(actor.getTranslatedMessageAsString(Messages.COMMAND_CONTAINER_INFO_CONTAINER_FRAME,
                    Key.of("itemName", ItemEditor.of(frame.getItem()).getItemName())));
                component.hover(new HoverEvent(HoverEvent.Action.SHOW_ITEM, plugin.versionHelper().createItemHover(frame.getItem())));
                component.send(actor);
            }
        }, () -> {
            actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ERROR_STORAGE_ACCESS_LEVEL, Key.of("level", world.getName()));
        });
    }

    private void doBlockContainer(JustLootItPlugin plugin, Block block, org.bukkit.block.Container stateContainer, World world, Actor<?> actor, int x, int y, int z) {
        PersistentDataContainer dataContainer = stateContainer.getPersistentDataContainer();
        Location location = block.getLocation();
        org.bukkit.block.Container otherContainer = stateContainer;
        if (!dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)
            && dataContainer.has(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR)) {
            if (!(stateContainer.getBlockData() instanceof Chest chest) || chest.getType() == Chest.Type.SINGLE) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_INFO_NO_CONTAINER_BLOCK, Key.of("x", x), Key.of("y", y),
                    Key.of("z", z), Key.of("world", world.getName()));
                dataContainer.remove(JustLootItKey.chestData());
                stateContainer.update(false, false);
                return;
            }
            Vector vector = dataContainer.get(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR);
            location.add(vector);
            block = world.getBlockAt(location);
            if (block.isEmpty()) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_INFO_NO_CONTAINER_BLOCK, Key.of("x", x), Key.of("y", y),
                    Key.of("z", z), Key.of("world", world.getName()));
                dataContainer.remove(JustLootItKey.chestData());
                stateContainer.update(false, false);
                return;
            }
            if (!(block.getState() instanceof org.bukkit.block.Container stateContainer0)) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_INFO_NO_CONTAINER_BLOCK, Key.of("x", x), Key.of("y", y),
                    Key.of("z", z), Key.of("world", world.getName()));
                dataContainer.remove(JustLootItKey.chestData());
                stateContainer.update(false, false);
                return;
            }
            if (!stateContainer0.getPersistentDataContainer().has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_INFO_NO_CONTAINER_BLOCK, Key.of("x", x), Key.of("y", y),
                    Key.of("z", z), Key.of("world", world.getName()));
                dataContainer.remove(JustLootItKey.chestData());
                stateContainer.update(false, false);
                return;
            }
            stateContainer = stateContainer0;
            dataContainer = stateContainer.getPersistentDataContainer();
        }
        if (!dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_INFO_NO_CONTAINER_BLOCK, Key.of("x", location.getBlockX()),
                Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", world.getName()));
            return;
        }
        long id = dataContainer.get(JustLootItKey.identity(), PersistentDataType.LONG);
        final org.bukkit.block.Container finalContainer = stateContainer;
        plugin.versionHandler().getLevel(world).getCapability(StorageCapability.class).ifPresentOrElse(capability -> {
            Container container = (Container) capability.storage().read(id);
            if (container == null) {
                PersistentDataContainer finalDataContainer = finalContainer.getPersistentDataContainer();
                if (finalContainer != otherContainer) {
                    otherContainer.getPersistentDataContainer().remove(JustLootItKey.chestData());
                    otherContainer.update(false, false);
                } else if (finalDataContainer.has(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR)) {
                    final Block finalBlock = world
                        .getBlockAt(location.add(finalDataContainer.get(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR)));
                    final BlockState finalState = finalBlock.getState();
                    if (finalState instanceof org.bukkit.block.Container finalOtherContainer) {
                        finalOtherContainer.getPersistentDataContainer().remove(JustLootItKey.chestData());
                        finalOtherContainer.update(false, false);
                    }
                }
                finalDataContainer.remove(JustLootItKey.chestData());
                finalDataContainer.remove(JustLootItKey.identity());
                finalContainer.update(false, false);
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_INFO_NO_CONTAINER_BLOCK, Key.of("x", location.getBlockX()),
                    Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", world.getName()));
                return;
            }
            String refreshGroup = container.getGroupId();
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_INFO_CONTAINER_ANY_BLOCK, Key.of("id", container.id()),
                Key.of("refreshGroup", refreshGroup == null ? "None" : refreshGroup), Key.of("type", TypeName.ofContainer(container)),
                Key.of("x", location.getBlockX()), Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()),
                Key.of("world", world.getName()));
            if (container instanceof VanillaContainer vanilla) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_INFO_CONTAINER_VANILLA, Key.of("seed", vanilla.getSeed()),
                    Key.of("lootTable", vanilla.getLootTableKey()));
            }
        }, () -> {
            actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ERROR_STORAGE_ACCESS_LEVEL, Key.of("level", world.getName()));
        });
    }

}
