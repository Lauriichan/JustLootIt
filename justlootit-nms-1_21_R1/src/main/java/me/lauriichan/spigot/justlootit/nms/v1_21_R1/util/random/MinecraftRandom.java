package me.lauriichan.spigot.justlootit.nms.v1_21_R1.util.random;

import me.lauriichan.spigot.justlootit.nms.IMinecraftRandom;
import me.lauriichan.spigot.justlootit.nms.IMinecraftRandomFactory;
import net.minecraft.util.RandomSource;

public final class MinecraftRandom implements IMinecraftRandom {

    private final RandomSource delegate;

    public MinecraftRandom(final RandomSource delegate) {
        this.delegate = delegate;
    }
    
    public RandomSource delegate() {
        return delegate;
    }

    @Override
    public IMinecraftRandom fork() {
        return new MinecraftRandom(delegate.fork());
    }

    @Override
    public IMinecraftRandomFactory forkPositional() {
        return new MinecraftRandomFactory(delegate.forkPositional());
    }

    @Override
    public void seed(long seed) {
        delegate.setSeed(seed);
    }

    @Override
    public int nextInt() {
        return delegate.nextInt();
    }

    @Override
    public int nextInt(int bound) {
        return delegate.nextInt(bound);
    }

    @Override
    public boolean nextBoolean() {
        return delegate.nextBoolean();
    }

    @Override
    public long nextLong() {
        return delegate.nextLong();
    }

    @Override
    public float nextFloat() {
        return delegate.nextFloat();
    }

    @Override
    public double nextDouble() {
        return delegate.nextDouble();
    }

    @Override
    public double nextGaussian() {
        return delegate.nextGaussian();
    }

}
