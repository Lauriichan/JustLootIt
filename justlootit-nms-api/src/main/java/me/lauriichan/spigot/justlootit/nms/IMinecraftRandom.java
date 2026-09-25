package me.lauriichan.spigot.justlootit.nms;

public interface IMinecraftRandom {

    IMinecraftRandom fork();

    IMinecraftRandomFactory forkPositional();
    
    void seed(long seed);

    int nextInt();

    int nextInt(int bound);

    default int nextInt(int origin, int bound) {
        return nextInt(bound) + origin;
    }

    default int nextIntInclusive(int min, int max) {
        return nextInt(max - min + 1) + min;
    }

    boolean nextBoolean();

    long nextLong();

    float nextFloat();

    double nextDouble();

    double nextGaussian();

    default double triangle(final double mean, final double spread) {
        return mean + spread * (nextDouble() - nextDouble());
    }

    default float triangle(final float mean, final float spread) {
        return mean + spread * (nextFloat() - nextFloat());
    }

    default void skip(int amount) {
        for (int i = 0; i < amount; i++) {
            nextInt();
        }
    }

}
