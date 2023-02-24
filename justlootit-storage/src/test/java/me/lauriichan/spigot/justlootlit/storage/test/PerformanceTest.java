package me.lauriichan.spigot.justlootlit.storage.test;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class PerformanceTest {

    public static final int WARMUP_ROUNDS = 3;
    public static final int SAMPLE_ROUNDS = 3;
    public static final int RUNS_PER_ROUND = 50;

    private static final double MILLI_IN_NANOS = TimeUnit.MILLISECONDS.toNanos(1);
    private static final DecimalFormat FORMAT = new DecimalFormat("0.000");

    private static final class Profiler {

        private final ArrayList<Long> durations = new ArrayList<>();
        private Long start = null;

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

    private static final class Result {

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

    @Test
    public static void runPerformanceTest() {
        Profiler profiler = new Profiler();
        profiler.lock();
        // Warumups
        for (int index = 0; index < WARMUP_ROUNDS; index++) {
            runTest(profiler);
        }
        profiler.unlock();
        // Samples
        Result[] results = new Result[SAMPLE_ROUNDS];
        for (int index = 0; index < SAMPLE_ROUNDS; index++) {
            runTest(profiler);
            results[index] = profiler.result();
        }
        // Evaluate

        BigInteger average = BigInteger.ZERO;
        long min = Long.MAX_VALUE;
        long max = 0;
        for (int index = 0; index < SAMPLE_ROUNDS; index++) {
            Result result = results[index];
            result.evaluate();
            System.out.println("Results for Round " + (index + 1) + "\n");
            print(result.min(), result.max(), result.average());
            System.out.println(" ");
        }
        System.out.println("\n\nOverall results:");
        print(min, max, average.divide(BigInteger.valueOf(SAMPLE_ROUNDS)).longValue());
    }

    private static void print(long min, long max, long average) {
        print("Minimum", min);
        print("Maximum", min);
        print("Average", min);
    }

    private static void print(String name, long value) {
        System.out.println(name + ": " + FORMAT.format(value / MILLI_IN_NANOS) + "ms");
    }

    /*
     * 
     */

    private static void runTest(Profiler profiler) {
        for (int index = 0; index < RUNS_PER_ROUND; index++) {
            profiler.time();
            testRandomAccessFileStorage();
//            testSimpleFileStorage();
//            testSQLiteStorage();
            profiler.time();
        }
    }

    /*
     * 
     */

    private static void testSQLiteStorage() {

    }

    private static void testSimpleFileStorage() {

    }

    private static void testRandomAccessFileStorage() {

    }

}
