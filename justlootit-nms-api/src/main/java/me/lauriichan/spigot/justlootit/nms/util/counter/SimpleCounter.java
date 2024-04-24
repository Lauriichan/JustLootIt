package me.lauriichan.spigot.justlootit.nms.util.counter;

public class SimpleCounter extends Counter {

    private final long max;
    private volatile long value = 0;
    
    public SimpleCounter(long max) {
        this.max = Math.max(max, 0);
    }

    @Override
    public long current() {
        return value;
    }

    @Override
    public long max() {
        return max;
    }

    @Override
    public void increment(long amount) {
        if (value + amount > max) {
            if (max != value) {
                value = max;
            }
            return;
        }
        value += amount;
    }

}
