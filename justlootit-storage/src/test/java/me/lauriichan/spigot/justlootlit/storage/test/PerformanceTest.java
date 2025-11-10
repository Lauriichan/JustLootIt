package me.lauriichan.spigot.justlootlit.storage.test;

import java.io.File;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import me.lauriichan.spigot.justlootlit.storage.test.Test.StorageProvider;
import me.lauriichan.spigot.justlootlit.storage.test.simple.*;

public class PerformanceTest {

    /*
     * ONLY MODIFY PUBLIC FIELDS
     */

    public static final long SEED = 285428738523L;

    public static final int RUNS_PER_ROUND = 3;

    public static final int WARMUP_RUNS = 2;
    public static final int SAMPLE_ROUNDS = 1;

    public static final boolean PRINT_EACH_ROUND = false;

    public static final Test[] TESTS = new Test[] {
        new WriteReadTest(1024),
        new WriteUpdateReadTest(1024),
        new ShuffledWriteReadTest(1024),
        new WriteReadDeleteTest(1024),
        new WriteOverwriteReadTest(1024)
    };

    /*
     * ONLY MODIFY PUBLIC FIELDS
     */

    private static final double MILLI_IN_NANOS = TimeUnit.MILLISECONDS.toNanos(1);
    private static final DecimalFormat FORMAT = new DecimalFormat("0.000");

    @Disabled
    @TestFactory
    public Collection<DynamicTest> performanceTests() {
        final ArrayList<DynamicTest> tests = new ArrayList<>(TESTS.length);
        if (TESTS.length == 0) {
            return tests;
        }
        for (final Test test : TESTS) {
            tests.add(DynamicTest.dynamicTest(test.name, () -> runTest(test)));
        }
        return tests;
    }
    
    private static void runTest(final Test test) throws Throwable {
        final ArrayList<StorageProvider> providerList = new ArrayList<>();
        test.createProviders(providerList);
        final StorageProvider[] providers = providerList.toArray(StorageProvider[]::new);
        providerList.clear();
        final File workingDir = new File("tests", test.name);
        final File warmupDir = new File(workingDir, "warmup");
        for (final StorageProvider provider : providers) {
            if (workingDir.exists()) {
                FileUtils.forceDelete(workingDir);
            }
            workingDir.mkdirs();
            final Profiler profiler = new Profiler(RUNS_PER_ROUND);
            profiler.lock();
            for (int index = 0; index < WARMUP_RUNS; index++) {
                warmupDir.mkdirs();
                test.executeTest(warmupDir, provider, profiler, SEED);
                FileUtils.forceDelete(warmupDir);
            }
            profiler.unlock();
            final Random seedRandom = new Random(SEED);
            final Profiler.Result[] results = new Profiler.Result[SAMPLE_ROUNDS];
            for (int index = 0; index < SAMPLE_ROUNDS; index++) {
                final File roundDir = new File(workingDir, "round" + index);
                roundDir.mkdirs();
                final long roundSeed = seedRandom.nextLong();
                for (int idx = 0; idx < RUNS_PER_ROUND; idx++) {
                    final File runDir = new File(roundDir, "run" + idx);
                    runDir.mkdirs();
                    test.executeTest(runDir, provider, profiler, roundSeed);
                }
                results[index] = profiler.result();
            }
            // Evaluate
            BigInteger average = BigInteger.ZERO;
            long min = Long.MAX_VALUE;
            long max = 0;
            for (int index = 0; index < SAMPLE_ROUNDS; index++) {
                final Profiler.Result result = results[index];
                result.evaluate();
                if (min > result.min()) {
                    min = result.min();
                }
                if (max < result.max()) {
                    max = result.max();
                }
                average = average.add(BigInteger.valueOf(result.average()));
                if (PRINT_EACH_ROUND) {
                    System.out.println("Results for Round " + (index + 1) + " of '" + provider.name + "'\n");
                    printResult(result.min(), result.max(), result.average());
                    System.out.println(" ");
                }
            }
            System.out.println("\n\nOverall results of '" + provider.name + "':");
            printResult(min, max, average.divide(BigInteger.valueOf(SAMPLE_ROUNDS)).longValue());
        }
    }

    private static void printResult(final long min, final long max, final long average) {
        printValue("Minimum", min);
        printValue("Maximum", max);
        printValue("Average", average);
    }

    private static void printValue(final String name, final long value) {
        System.out.println(name + ": " + FORMAT.format(value / MILLI_IN_NANOS) + "ms");
    }

}
