package me.lauriichan.spigot.justlootit.nms.v1_19_R2.util;

import java.util.Optional;
import java.util.function.BiFunction;

import org.bukkit.Location;

import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;

public final class MinecraftConstant1_19_R2 {

    private MinecraftConstant1_19_R2() {
        throw new UnsupportedOperationException();
    }

    public static final ContainerLevelAccess BETTER_NULL = new ContainerLevelAccess() {

        @Override
        public final <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> var1) {
            return Optional.empty();
        }

        public final Location getLocation() {
            return new Location(null, 0, 0, 0);
        }

    };

}
