package me.lauriichan.spigot.justlootit.storage.util.counter;

public class ScaledCounter extends Counter {

    private final Counter delegate;
    private final long max;
    private volatile long value = 0;

    private final long maxDelegate;
    private volatile long contributed = 0;

    public ScaledCounter(Counter delegate, long max) {
        this(delegate, max, delegate.max() - delegate.current());
    }

    public ScaledCounter(Counter delegate, long max, long maxDelegate) {
        this.delegate = delegate;
        this.max = Math.max(max, 0);
        this.maxDelegate = Math.max(maxDelegate, 0);
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
        try {
            if (value + amount > max) {
                if (max != value) {
                    value = max;
                }
                return;
            }
            value += amount;
        } finally {
            double percent = max / ((double) value);
            long progress = (long) Math.floor(maxDelegate * percent);
            if (progress > contributed) {
                delegate.increment(progress - contributed);
                contributed = progress;
            }
        }
    }

}
