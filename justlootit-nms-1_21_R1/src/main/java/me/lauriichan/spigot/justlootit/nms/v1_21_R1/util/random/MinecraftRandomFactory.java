package me.lauriichan.spigot.justlootit.nms.v1_21_R1.util.random;

import me.lauriichan.spigot.justlootit.nms.IMinecraftRandom;
import me.lauriichan.spigot.justlootit.nms.IMinecraftRandomFactory;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

public final class MinecraftRandomFactory implements IMinecraftRandomFactory {

    private final PositionalRandomFactory delegate;

    public MinecraftRandomFactory(final PositionalRandomFactory delegate) {
        this.delegate = delegate;
    }
    
    public PositionalRandomFactory delegate() {
        return delegate;
    }

    @Override
    public IMinecraftRandom at(int x, int y, int z) {
        return new MinecraftRandom(delegate.at(x, y, z));
    }

    @Override
    public IMinecraftRandom fromHashOf(String name) {
        return new MinecraftRandom(delegate.fromHashOf(name));
    }

    @Override
    public IMinecraftRandom fromSeed(long seed) {
        return new MinecraftRandom(delegate.fromSeed(seed));
    }

}
