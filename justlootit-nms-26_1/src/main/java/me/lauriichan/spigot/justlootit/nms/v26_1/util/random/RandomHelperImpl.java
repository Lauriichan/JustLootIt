package me.lauriichan.spigot.justlootit.nms.v26_1.util.random;

import me.lauriichan.spigot.justlootit.nms.IMinecraftRandom;
import me.lauriichan.spigot.justlootit.nms.RandomHelper;
import net.minecraft.util.RandomSource;

public final class RandomHelperImpl extends RandomHelper {
    
    public static final RandomHelperImpl INSTANCE = new RandomHelperImpl();
    
    private RandomHelperImpl() {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public IMinecraftRandom newRandom() {
        return new MinecraftRandom(RandomSource.create());
    }

    @Override
    public IMinecraftRandom newRandom(long seed) {
        return new MinecraftRandom(RandomSource.create(seed));
    }

    @Override
    public IMinecraftRandom newThreadLocalRandom() {
        return new MinecraftRandom(RandomSource.createThreadLocalInstance());
    }

    @Override
    public IMinecraftRandom newThreadLocalRandom(long seed) {
        return new MinecraftRandom(RandomSource.createThreadLocalInstance(seed));
    }

}
