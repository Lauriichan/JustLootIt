package me.lauriichan.spigot.justlootit.nms.util.counter;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class CompositeCounter extends Counter {
    
    private final ObjectArrayList<Counter> counters = new ObjectArrayList<>();
    private volatile long max = 0L;
    
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
        return value;
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
