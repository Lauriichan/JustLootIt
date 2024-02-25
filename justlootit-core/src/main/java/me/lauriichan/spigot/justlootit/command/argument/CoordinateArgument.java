package me.lauriichan.spigot.justlootit.command.argument;

import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.IArgumentMap;
import me.lauriichan.laylib.command.IArgumentType;
import me.lauriichan.laylib.command.Suggestions;
import me.lauriichan.laylib.command.exception.ArgumentStack;

public class CoordinateArgument implements IArgumentType<CoordinateArgument.Coord> {

    public static record Coord(int value) {}

    private final Axis axis;

    public CoordinateArgument(IArgumentMap map) {
        ArgumentStack stack = new ArgumentStack();
        String axisRaw = map.getOrStack("axis", String.class, stack);
        Axis axis = null;
        if (axisRaw != null) {
            axis = Axis.valueOf(axisRaw.toUpperCase());
        }
        this.axis = axis;
        stack.throwIfPresent();
    }

    @Override
    public Coord parse(Actor<?> actor, String input, IArgumentMap map) throws IllegalArgumentException {
        if (input.contains("~")) {
            if (!(actor.getHandle() instanceof Entity entity)) {
                throw new IllegalArgumentException("Only an ingame entity can use relative coordinates.");
            }
            int index = input.indexOf("~");
            if (index > 1) {
                throw new IllegalArgumentException("Invalid relative position: '" + input + "'");
            }
            Location location;
            int requiredLength = input.charAt(0) != '~' ? 2 : 1;
            if (requiredLength == 2 && entity instanceof HumanEntity human) {
                if (input.charAt(0) == '#') {
                    Block block = human.getTargetBlockExact(getBlockReach(human));
                    if (block == null) {
                        throw new IllegalArgumentException(
                            "Can't retrieve looking at coordinates because player is not looking at a block.");
                    }
                    location = block.getLocation();
                } else if (input.charAt(0) == '!' && axis == Axis.Y) {
                    location = human.getEyeLocation();
                } else {
                    throw new IllegalArgumentException("Invalid relative position: '" + input + "'");
                }
            } else {
                location = entity.getLocation();
            }
            if (requiredLength == input.length()) {
                return new Coord(getBlock(location));
            }
            String addition = input.substring(index + 1);
            int base = 1;
            boolean offset = false;
            char ch = addition.charAt(0);
            if (ch == '-') {
                base = -1;
                offset = true;
            } else if (ch == '+') {
                offset = true;
            } else if (!Character.isDigit(ch)) {
                throw new IllegalArgumentException("Unknown sign '" + ch + "'");
            }
            String number = offset ? addition.substring(1) : addition;
            try {
                int value = base * Integer.parseInt(number);
                return new Coord(getBlock(location) + value);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Unable to parse number '" + number + "'");
            }
        }
        try {
            return new Coord(Integer.parseInt(input));
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Unable to parse number '" + input + "'");
        }
    }

    @Override
    public void suggest(Actor<?> actor, String input, Suggestions suggestions, IArgumentMap map) {
        try {
            suggestions.suggest(1, Integer.parseInt(input));
        } catch (NumberFormatException nfe) {
            // Ignore
        }
        if (!(actor.getHandle() instanceof Entity)) {
            return;
        }
        if (input.isBlank()) {
            suggestions.suggest(0, "~");
            if (actor.getHandle() instanceof HumanEntity) {
                suggestions.suggest(0, "#~");
            }
        }
        if (input.contains("~")) {
            suggestions.suggest(1, input);
        }
        map.get("location", Location.class).ifPresent(loc -> {
            suggestions.suggest(0, getBlock(loc));
        });

    }

    private int getBlock(Location location) {
        switch (axis) {
        case X:
            return location.getBlockX();
        case Y:
            return location.getBlockY();
        default:
            return location.getBlockZ();
        }
    }

    public static int getBlockReach(HumanEntity entity) {
        return 5;
    }

}
