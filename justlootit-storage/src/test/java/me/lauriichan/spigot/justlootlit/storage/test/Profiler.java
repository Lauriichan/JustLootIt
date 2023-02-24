package me.lauriichan.spigot.justlootlit.storage.test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Profiler {
    
    public static final class Result {

        private final List<Long> durations;

        private final int amount;

        private long average = 0;
        private long min = 0;
        private long max = 0;

        public Result(List<Long> durations) {
            this.durations = Collections.unmodifiableList(new ArrayList<>(durations));
            this.amount = durations.size();
        }

        public long average() {
            return average;
        }

        public long min() {
            return min;
        }

        public long max() {
            return max;
        }

        public void evaluate() {
            if (average != 0 || min != 0 || max != 0 || amount == 0) {
                return;
            }
            BigInteger average = BigInteger.ZERO;
            long min = Long.MAX_VALUE;
            long max = 0;
            for (int index = 0; index < amount; index++) {
                long duration = durations.get(index);
                if (duration > max) {
                    max = duration;
                }
                if (duration < min) {
                    min = duration;
                }
                average.add(BigInteger.valueOf(duration));
            }
            this.min = min;
            this.max = max;
            this.average = average.divide(BigInteger.valueOf(amount)).longValue();
        }

    }

    private final ArrayList<Long> durations;
    private Long start = null;

    public Profiler(final int runs) {
        this.durations = new ArrayList<>(runs);
    }

    private boolean locked = false;

    public void lock() {
        locked = true;
    }

    public void unlock() {
        locked = false;
    }

    public void time() {
        long time = System.nanoTime();
        if (locked) {
            return;
        }
        if (start != null) {
            durations.add(time - start);
            start = null;
            return;
        }
        start = time;
    }

    public Result result() {
        if (locked) {
            throw new IllegalStateException("Unsupported when locked");
        }
        Result result = new Result(durations);
        durations.clear();
        return result;
    }

}