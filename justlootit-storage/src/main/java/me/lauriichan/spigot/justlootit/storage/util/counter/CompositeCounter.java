package me.lauriichan.spigot.justlootit.storage.util.counter;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class CompositeCounter extends Counter {
    
    private final ObjectArrayList<Counter> counters = new ObjectArrayList<>();
    private volatile long max = 0L, cachedCurrent = 0L;
    
    public void add(Counter counter) {
        if (counters.contains(counter) || counter instanceof CompositeCounter) {
            return;
        }
        counters.add(counter);
        max += counter.max();
    }

    @Override
    public long current() {
        long value = 0;
        for (Counter counter : counters) {
            value += counter.current();
        }
        return cachedCurrent = value;
    }
    
    @Override
    public double progress() {
        if (max == 0) {
            return 1d;
        }
        return ((double) cachedCurrent) / ((double) max);
    }

    @Override
    public long max() {
        return max;
    }

    @Override
    public void increment(long amount) {
        // Composite counter will never increase on its own
    }

}
