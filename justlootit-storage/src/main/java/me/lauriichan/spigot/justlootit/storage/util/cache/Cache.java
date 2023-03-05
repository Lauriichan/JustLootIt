package me.lauriichan.spigot.justlootit.storage.util.cache;

public abstract class Cache {
    
    private volatile long cacheTime = 1;
    private volatile boolean tickPaused = false;
    
    public final void tickPaused(boolean tickPaused) {
        this.tickPaused = tickPaused;
    }
    
    public final boolean tickPaused() {
        return tickPaused;
    }

    public final void cacheTime(long cacheTime) {
        this.cacheTime = Math.max(cacheTime, 1);
    }

    public final long cacheTime() {
        return cacheTime;
    }
    
    final void tick() {
        if(tickPaused || !canTick()) {
            return;
        }
        onTick(cacheTime);
    }
    
    protected abstract boolean canTick();
    
    protected abstract void onTick(final long cacheTime);

}
