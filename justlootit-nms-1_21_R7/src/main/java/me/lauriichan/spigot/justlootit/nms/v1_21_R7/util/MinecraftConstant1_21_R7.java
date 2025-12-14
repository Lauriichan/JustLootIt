package me.lauriichan.spigot.justlootit.nms.v1_21_R7.util;

import java.util.Optional;
import java.util.function.BiFunction;

import org.bukkit.Location;

import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;

public final class MinecraftConstant1_21_R7 {

    private MinecraftConstant1_21_R7() {
        throw new UnsupportedOperationException();
    }

    public static final ContainerLevelAccess BETTER_NULL = new ContainerLevelAccess() {

        @Override
        public final <T> Optional<T> evaluate(final BiFunction<Level, BlockPos, T> var1) {
            return Optional.empty();
        }

        @Override
        public final Location getLocation() {
            return new Location(null, 0, 0, 0);
        }

    };

}