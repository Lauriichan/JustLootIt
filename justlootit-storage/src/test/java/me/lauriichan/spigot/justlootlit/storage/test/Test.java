package me.lauriichan.spigot.justlootlit.storage.test;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.storage.Storage;

public abstract class Test<T extends Storable> {

    public static final class StorageProvider<T extends Storable> {

        public static <T extends Storable> StorageProvider<T> provider(final String name, final Function<File, Storage<T>> builder) {
            return new StorageProvider<>(name, builder);
        }

        public final String name;
        private final Function<File, Storage<T>> builder;

        private StorageProvider(final String name, final Function<File, Storage<T>> builder) {
            this.name = name;
            this.builder = builder;
        }

    }

    public final String name;

    public Test(final String name) {
        this.name = name;
    }

    public abstract void createProviders(List<StorageProvider<T>> list);

    public final void executeTest(File workingDir, StorageProvider<T> provider, Profiler profiler, long seed) throws Throwable {
        Storage<T> storage = provider.builder.apply(workingDir);
        Random random = new Random(seed);
        profiler.time();
        executeTest(provider.name, storage, random);
        profiler.time();
        storage.close();
    }

    protected abstract void executeTest(String storageName, Storage<T> storage, Random random) throws Throwable;

}
