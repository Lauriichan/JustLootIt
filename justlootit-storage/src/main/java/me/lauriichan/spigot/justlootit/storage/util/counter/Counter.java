package me.lauriichan.spigot.justlootit.storage.util.counter;

public abstract class Counter {
    
    public abstract long current();
    
    public abstract long max();
    
    public void increment() {
        increment(1);
    }
    
    public abstract void increment(long amount);
    
    public void finish() {
        increment(max() - current());
    }
    
    public double progress() {
        long max = max();
        if (max == 0) {
            return 1d;
        }
        return ((double) current()) / ((double) max);
    }

}
