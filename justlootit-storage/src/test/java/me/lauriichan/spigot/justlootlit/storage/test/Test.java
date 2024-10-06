package me.lauriichan.spigot.justlootlit.storage.test;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import me.lauriichan.spigot.justlootit.storage.Storage;

public abstract class Test {

    public static final class StorageProvider {

        public static StorageProvider provider(final String name, final Function<File, Storage> builder) {
            return new StorageProvider(name, builder);
        }

        public final String name;
        private final Function<File, Storage> builder;

        private StorageProvider(final String name, final Function<File, Storage> builder) {
            this.name = name;
            this.builder = builder;
        }

    }

    public final String name;

    public Test(final String name) {
        this.name = name;
    }

    public abstract void createProviders(List<StorageProvider> list);

    public final void executeTest(final File workingDir, final StorageProvider provider, final Profiler profiler, final long seed)
        throws Throwable {
        final Storage storage = provider.builder.apply(workingDir);
        final Random random = new Random(seed);
        profiler.time();
        executeTest(provider.name, storage, random);
        profiler.time();
        storage.close();
    }

    protected abstract void executeTest(String storageName, Storage storage, Random random) throws Throwable;

}
