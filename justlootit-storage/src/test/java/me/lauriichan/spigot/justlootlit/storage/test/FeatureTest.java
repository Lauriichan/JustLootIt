package me.lauriichan.spigot.justlootlit.storage.test;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootlit.storage.test.Test.StorageProvider;
import me.lauriichan.spigot.justlootlit.storage.test.simple.*;

public class FeatureTest {

    /*
     * ONLY MODIFY PUBLIC FIELDS
     */
    
    public static final long SEED = 285428738523L;

    public static final Test<?>[] TESTS = new Test[] {
        new WriteReadTest(1024),
        new WriteReadDeleteTest(1024),
        new WriteUpdateReadTest(1024),
    };

    /*
     * ONLY MODIFY PUBLIC FIELDS
     */

    @TestFactory
    public Collection<DynamicTest> featureTests() {
        ArrayList<DynamicTest> tests = new ArrayList<>(TESTS.length);
        if (TESTS.length == 0) {
            return tests;
        }
        for (Test<?> test : TESTS) {
            createTests(tests, test);
        }
        return tests;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Storable> void createTests(ArrayList<DynamicTest> collection, Test<T> test) {
        ArrayList<StorageProvider<T>> providerList = new ArrayList<>();
        test.createProviders(providerList);
        StorageProvider<T>[] providers = providerList.toArray(StorageProvider[]::new);
        providerList.clear();
        File workingDir = new File("tests/features", test.name);
        if (workingDir.exists()) {
            try {
                FileUtils.forceDelete(workingDir);
            } catch (IOException e) {
                fail(e);
            }
        }
        workingDir.mkdirs();
        Profiler profiler = new Profiler(0);
        profiler.lock();
        for (StorageProvider<T> provider : providers) {
            File storageDir = new File(workingDir, provider.name);
            storageDir.mkdirs();
            collection.add(DynamicTest.dynamicTest(test.name + " [" + provider.name + "]", () -> test.executeTest(storageDir, provider, profiler, SEED)));
        }
        profiler.unlock();
    }

}
