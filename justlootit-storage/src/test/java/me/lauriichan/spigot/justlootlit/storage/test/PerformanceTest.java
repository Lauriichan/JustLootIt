package me.lauriichan.spigot.justlootlit.storage.test;

import java.io.File;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootlit.storage.test.Test.StorageProvider;

public class PerformanceTest {
    
    /*
     * ONLY MODIFY PUBLIC FIELDS
     */

    public static final int RUNS_PER_ROUND = 50;

    public static final int WARMUP_ROUNDS = 3;
    public static final int SAMPLE_ROUNDS = 3;

    public static final Test<?>[] TESTS = new Test[] {

    };
    
    /*
     * ONLY MODIFY PUBLIC FIELDS
     */

    private static final double MILLI_IN_NANOS = TimeUnit.MILLISECONDS.toNanos(1);
    private static final DecimalFormat FORMAT = new DecimalFormat("0.000");

    private static final int WARUMUP_RUNS = RUNS_PER_ROUND * WARMUP_ROUNDS;

    @TestFactory
    public static Collection<DynamicTest> performanceTests() {
        ArrayList<DynamicTest> tests = new ArrayList<>(TESTS.length);
        if (TESTS.length == 0) {
            return tests;
        }
        for (Test<?> test : TESTS) {
            tests.add(DynamicTest.dynamicTest(test.name, () -> runTest(test)));
        }
        return tests;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Storable> void runTest(Test<T> test) throws Throwable {
        ArrayList<StorageProvider<T>> providerList = new ArrayList<>();
        test.createProviders(providerList);
        StorageProvider<T>[] providers = providerList.toArray(StorageProvider[]::new);
        providerList.clear();
        File workingDir = new File("tests", test.name);
        FileUtils.forceDelete(workingDir);
        workingDir.mkdirs();
        File warmupDir = new File(workingDir, "warmup");
        for (StorageProvider<T> provider : providers) {
            Profiler profiler = new Profiler(RUNS_PER_ROUND);
            profiler.lock();
            for (int index = 0; index < WARUMUP_RUNS; index++) {
                warmupDir.mkdirs();
                test.executeTest(warmupDir, provider, profiler);
                FileUtils.forceDelete(warmupDir);
            }
            profiler.unlock();
            Profiler.Result[] results = new Profiler.Result[SAMPLE_ROUNDS];
            for (int index = 0; index < SAMPLE_ROUNDS; index++) {
                File roundDir = new File(workingDir, "round" + index);
                roundDir.mkdirs();
                for (int idx = 0; idx < RUNS_PER_ROUND; idx++) {
                    File runDir = new File(roundDir, "run" + idx);
                    runDir.mkdirs();
                    test.executeTest(runDir, provider, profiler);
                }
                results[index] = profiler.result();
            }
            // Evaluate
            BigInteger average = BigInteger.ZERO;
            long min = Long.MAX_VALUE;
            long max = 0;
            for (int index = 0; index < SAMPLE_ROUNDS; index++) {
                Profiler.Result result = results[index];
                result.evaluate();
                System.out.println("Results for Round " + (index + 1) + " of '" + provider.name + "'\n");
                printResult(result.min(), result.max(), result.average());
                System.out.println(" ");
            }
            System.out.println("\n\nOverall results of '" + provider.name + "':");
            printResult(min, max, average.divide(BigInteger.valueOf(SAMPLE_ROUNDS)).longValue());
        }
    }

    private static void printResult(long min, long max, long average) {
        printValue("Minimum", min);
        printValue("Maximum", min);
        printValue("Average", min);
    }

    private static void printValue(String name, long value) {
        System.out.println(name + ": " + FORMAT.format(value / MILLI_IN_NANOS) + "ms");
    }

}
