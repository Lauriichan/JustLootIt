package me.lauriichan.spigot.justlootit.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.spigot.justlootit.command.argument.CoordinateArgument;
import me.lauriichan.spigot.justlootit.command.argument.CoordinateArgument.Coord;
import me.lauriichan.spigot.justlootit.message.Messages;

public final class CommandUtil {

    private CommandUtil() {
        throw new UnsupportedOperationException();
    }
    
    public static Location getLocation(Actor<?> actor, Coord x, Coord y, Coord z, World world) {
        if (x == null || y == null || z == null) {
            if (!(actor.getHandle() instanceof HumanEntity entity)) {
                actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ACTOR_COORDS_REQUIRED_NON_PLAYER);
                return null;
            }
            Block block = entity.getTargetBlockExact(CoordinateArgument.getBlockReach(entity));
            if (block == null) {
                actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ACTOR_COORDS_REQUIRED_PLAYER);
                return null;
            }
            Location location = block.getLocation().clone();
            if (x != null) {
                location.setX(x.value());
            }
            if (y != null) {
                location.setY(y.value());
            }
            if (z != null) {
                location.setZ(z.value());
            }
            if (world != null) {
                location.setWorld(world);
            }
            return location;
        } else if (world == null) {
            if (!(actor.getHandle() instanceof Entity entity)) {
                actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ACTOR_WORLD_REQUIRED);
                return null;
            }
            world = entity.getWorld();
        }
        return new Location(world, x.value(), y.value(), z.value());
    }
    
}
