package me.lauriichan.spigot.justlootlit.storage.test;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import me.lauriichan.spigot.justlootlit.storage.test.Test.StorageProvider;
import me.lauriichan.spigot.justlootlit.storage.test.simple.*;

public class FeatureTest {

    /*
     * ONLY MODIFY PUBLIC FIELDS
     */

    public static final long SEED = 285428738523L;

    public static final Test[] TESTS = new Test[] {
        new WriteReadTest(128),
        new WriteReadDeleteTest(128),
        new WriteUpdateReadTest(128),
        new ShuffledWriteReadTest(128),
        new WriteOverwriteReadTest(128)
    };

    /*
     * ONLY MODIFY PUBLIC FIELDS
     */

    @Disabled
    @TestFactory
    public Collection<DynamicTest> featureTests() {
        final ArrayList<DynamicTest> tests = new ArrayList<>(TESTS.length);
        if (TESTS.length == 0) {
            return tests;
        }
        for (final Test test : TESTS) {
            createTests(tests, test);
        }
        return tests;
    }

    private static void createTests(final ArrayList<DynamicTest> collection, final Test test) {
        final ArrayList<StorageProvider> providerList = new ArrayList<>();
        test.createProviders(providerList);
        final StorageProvider[] providers = providerList.toArray(StorageProvider[]::new);
        providerList.clear();
        final File workingDir = new File("tests/features", test.name);
        if (workingDir.exists()) {
            try {
                FileUtils.forceDelete(workingDir);
            } catch (final IOException e) {
                fail(e);
            }
        }
        workingDir.mkdirs();
        final Profiler profiler = new Profiler(0);
        profiler.lock();
        for (final StorageProvider provider : providers) {
            final File storageDir = new File(workingDir, provider.name);
            storageDir.mkdirs();
            collection.add(DynamicTest.dynamicTest(test.name + " [" + provider.name + "]",
                () -> test.executeTest(storageDir, provider, profiler, SEED)));
        }
        profiler.unlock();
    }

}
