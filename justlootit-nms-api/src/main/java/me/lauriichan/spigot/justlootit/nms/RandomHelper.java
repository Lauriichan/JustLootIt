package me.lauriichan.spigot.justlootit.nms;

public abstract class RandomHelper {
    
    public abstract IMinecraftRandom newRandom();
    
    public abstract IMinecraftRandom newRandom(long seed);
    
    public abstract IMinecraftRandom newThreadLocalRandom();
    
    public abstract IMinecraftRandom newThreadLocalRandom(long seed);

}
