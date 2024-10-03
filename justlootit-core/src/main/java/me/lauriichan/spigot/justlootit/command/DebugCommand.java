package me.lauriichan.spigot.justlootit.command;

import static me.lauriichan.spigot.justlootit.JustLootItAccess.*;

import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.annotation.Action;
import me.lauriichan.laylib.command.annotation.Argument;
import me.lauriichan.laylib.command.annotation.Command;
import me.lauriichan.laylib.command.annotation.Description;
import me.lauriichan.laylib.command.annotation.Param;
import me.lauriichan.laylib.command.annotation.Permission;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.JustLootItPermission;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.command.argument.CoordinateArgument.Coord;
import me.lauriichan.spigot.justlootit.message.Messages;
import me.lauriichan.spigot.justlootit.util.CommandUtil;
import me.lauriichan.spigot.justlootit.util.EntityUtil;

@Extension
@Command(name = "debug")
@Permission(JustLootItPermission.COMMAND_DEBUG)
public class DebugCommand implements ICommandExtension {

    @Action("pdc")
    @Description("$#command.description.justlootit.debug.pdc")
    public void pdc(final JustLootItPlugin plugin, final Actor<?> actor,
        @Argument(name = "check entity", optional = true, index = 0) final boolean checkEntity,
        @Argument(name = "x", optional = true, index = 1, params = @Param(name = "axis", stringValue = "x", type = Param.TYPE_STRING)) final Coord x,
        @Argument(name = "y", optional = true, index = 2, params = @Param(name = "axis", stringValue = "y", type = Param.TYPE_STRING)) final Coord y,
        @Argument(name = "z", optional = true, index = 3, params = @Param(name = "axis", stringValue = "z", type = Param.TYPE_STRING)) final Coord z,
        @Argument(name = "world", optional = true, index = 4) World world) {
        final Location loc = CommandUtil.getLocation(actor, x, y, z, world);
        plugin.scheduler().syncRegional(loc, () -> {
            Block block = loc.getBlock();
            if (checkEntity || block.isEmpty()) {
                Collection<Entity> entities = loc.getWorld().getNearbyEntities(
                    new Location(loc.getWorld(), loc.getBlockX() + 0.5d, loc.getBlockY() + 0.5d, loc.getBlockZ() + 0.5d), 1.5d, 1.5d, 1.5d);
                if (entities.isEmpty()) {
                    actor.sendTranslatedMessage(Messages.COMMAND_DEBUG_PDC_DATA_EMPTY_BLOCK, Key.of("x", loc.getBlockX()),
                        Key.of("y", loc.getBlockY()), Key.of("z", loc.getBlockZ()), Key.of("world", loc.getWorld().getName()));
                    return;
                }
                List<Entity> validEntities = entities.stream()
                    .filter(entity -> EntityUtil.isSupportedEntity(entity) || EntityUtil.isItemFrame(entity)).toList();
                if (validEntities.isEmpty()) {
                    actor.sendTranslatedMessage(Messages.COMMAND_DEBUG_PDC_DATA_EMPTY_BLOCK, Key.of("x", loc.getBlockX()),
                        Key.of("y", loc.getBlockY()), Key.of("z", loc.getBlockZ()), Key.of("world", loc.getWorld().getName()));
                    return;
                }
                double distance = Double.MAX_VALUE;
                Location origin = new Location(loc.getWorld(), loc.getBlockX() + 0.5d, loc.getBlockY() + 0.5d, loc.getBlockZ() + 0.5d);
                Entity closest = null;
                for (Entity entity : validEntities) {
                    Location current = entity.getLocation();
                    double dist = origin.distanceSquared(current);
                    if (dist < distance) {
                        closest = entity;
                        distance = dist;
                    }
                }
                Location closestLoc = closest.getLocation();
                String data = plugin.versionHandler().debugHelper().persistentDataAsString(closest.getPersistentDataContainer());
                if (data.isEmpty()) {
                    actor.sendTranslatedMessage(Messages.COMMAND_DEBUG_PDC_DATA_EMPTY_ENTITY, Key.of("x", loc.getX()),
                        Key.of("y", closestLoc.getY()), Key.of("z", closestLoc.getZ()), Key.of("world", closestLoc.getWorld().getName()));
                    return;
                }
                actor.sendTranslatedMessage(Messages.COMMAND_DEBUG_PDC_DATA_FORMAT_ENTITY, Key.of("data", data.replace("\r", "")),
                    Key.of("x", closestLoc.getX()), Key.of("y", closestLoc.getY()), Key.of("z", closestLoc.getZ()),
                    Key.of("world", closestLoc.getWorld().getName()));
                return;
            }
            BlockState state = block.getState();
            if (!(state instanceof PersistentDataHolder dataHolder)) {
                actor.sendTranslatedMessage(Messages.COMMAND_DEBUG_PDC_DATA_EMPTY_BLOCK, Key.of("x", loc.getBlockX()),
                    Key.of("y", loc.getBlockY()), Key.of("z", loc.getBlockZ()), Key.of("world", loc.getWorld().getName()));
                return;
            }
            String data = plugin.versionHandler().debugHelper().persistentDataAsString(dataHolder.getPersistentDataContainer());
            if (data.isEmpty()) {
                actor.sendTranslatedMessage(Messages.COMMAND_DEBUG_PDC_DATA_EMPTY_BLOCK, Key.of("x", loc.getBlockX()),
                    Key.of("y", loc.getBlockY()), Key.of("z", loc.getBlockZ()), Key.of("world", loc.getWorld().getName()));
                return;
            }
            actor.sendTranslatedMessage(Messages.COMMAND_DEBUG_PDC_DATA_FORMAT_BLOCK, Key.of("data", data.replace("\r", "")),
                Key.of("x", loc.getBlockX()), Key.of("y", loc.getBlockY()), Key.of("z", loc.getBlockZ()),
                Key.of("world", loc.getWorld().getName()));
        });
    }

    @Action("jlidata")
    @Description("$#command.description.justlootit.debug.jlidata")
    public void jlidata(final JustLootItPlugin plugin, final Actor<?> actor,
        @Argument(name = "check entity", optional = true, index = 0) final boolean checkEntity,
        @Argument(name = "x", optional = true, index = 1, params = @Param(name = "axis", stringValue = "x", type = Param.TYPE_STRING)) final Coord x,
        @Argument(name = "y", optional = true, index = 2, params = @Param(name = "axis", stringValue = "y", type = Param.TYPE_STRING)) final Coord y,
        @Argument(name = "z", optional = true, index = 3, params = @Param(name = "axis", stringValue = "z", type = Param.TYPE_STRING)) final Coord z,
        @Argument(name = "world", optional = true, index = 4) World world) {
        final Location loc = CommandUtil.getLocation(actor, x, y, z, world);
        plugin.scheduler().syncRegional(loc, () -> {
            Block block = loc.getBlock();
            if (checkEntity || block.isEmpty()) {
                Collection<Entity> entities = loc.getWorld().getNearbyEntities(
                    new Location(loc.getWorld(), loc.getBlockX() + 0.5d, loc.getBlockY() + 0.5d, loc.getBlockZ() + 0.5d), 1.5d, 1.5d, 1.5d);
                if (entities.isEmpty()) {
                    actor.sendTranslatedMessage(Messages.COMMAND_DEBUG_PDC_DATA_EMPTY_BLOCK, Key.of("x", loc.getBlockX()),
                        Key.of("y", loc.getBlockY()), Key.of("z", loc.getBlockZ()), Key.of("world", loc.getWorld().getName()));
                    return;
                }
                List<Entity> validEntities = entities.stream()
                    .filter(entity -> EntityUtil.isSupportedEntity(entity) || EntityUtil.isItemFrame(entity)).toList();
                if (validEntities.isEmpty()) {
                    actor.sendTranslatedMessage(Messages.COMMAND_DEBUG_PDC_DATA_EMPTY_BLOCK, Key.of("x", loc.getBlockX()),
                        Key.of("y", loc.getBlockY()), Key.of("z", loc.getBlockZ()), Key.of("world", loc.getWorld().getName()));
                    return;
                }
                double distance = Double.MAX_VALUE;
                Location origin = new Location(loc.getWorld(), loc.getBlockX() + 0.5d, loc.getBlockY() + 0.5d, loc.getBlockZ() + 0.5d);
                Entity closest = null;
                for (Entity entity : validEntities) {
                    Location current = entity.getLocation();
                    double dist = origin.distanceSquared(current);
                    if (dist < distance) {
                        closest = entity;
                        distance = dist;
                    }
                }
                Location closestLoc = closest.getLocation();
                PersistentDataContainer container = closest.getPersistentDataContainer();
                if (container.isEmpty() || !(hasIdentity(container) || hasAnyOffset(container))) {
                    actor.sendTranslatedMessage(Messages.COMMAND_DEBUG_JLIDATA_EMPTY_ENTITY, Key.of("x", loc.getX()),
                        Key.of("y", closestLoc.getY()), Key.of("z", closestLoc.getZ()), Key.of("world", closestLoc.getWorld().getName()));
                    return;
                }
                actor.sendTranslatedMessage(Messages.COMMAND_DEBUG_JLIDATA_FORMAT_ENTITY,
                    getIdentityKey("id", container, "N/A"),
                    getBreakDataKey("break", container, "N/A"), Key.of("x", closestLoc.getX()),
                    Key.of("y", closestLoc.getY()), Key.of("z", closestLoc.getZ()), Key.of("world", closestLoc.getWorld().getName()));
                return;
            }
            BlockState state = block.getState();
            if (!(state instanceof PersistentDataHolder dataHolder)) {
                actor.sendTranslatedMessage(Messages.COMMAND_DEBUG_JLIDATA_EMPTY_BLOCK, Key.of("x", loc.getBlockX()),
                    Key.of("y", loc.getBlockY()), Key.of("z", loc.getBlockZ()), Key.of("world", loc.getWorld().getName()));
                return;
            }
            PersistentDataContainer container = dataHolder.getPersistentDataContainer();
            if (container.isEmpty() || !(hasIdentity(container) || hasAnyOffset(container) || hasBreakData(container))) {
                actor.sendTranslatedMessage(Messages.COMMAND_DEBUG_JLIDATA_EMPTY_BLOCK, Key.of("x", loc.getBlockX()),
                    Key.of("y", loc.getBlockY()), Key.of("z", loc.getBlockZ()), Key.of("world", loc.getWorld().getName()));
                return;
            }

            actor.sendTranslatedMessage(Messages.COMMAND_DEBUG_JLIDATA_FORMAT_BLOCK,
                getIdentityKey("id", container, "N/A"),
                getLegacyOffsetKey("legacy.offset", container, "N/A"),
                getOffsetV1Key("old.offset", container, "N/A"),
                getOffsetKey("offset", container, "N/A"),
                getBreakDataKey("break", container, "N/A"), Key.of("x", loc.getBlockX()),
                Key.of("y", loc.getBlockY()), Key.of("z", loc.getBlockZ()), Key.of("world", loc.getWorld().getName()));
        });
    }

}
