package me.lauriichan.spigot.justlootlit.storage.test;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import me.lauriichan.spigot.justlootit.storage.AbstractStorage;
import me.lauriichan.spigot.justlootit.storage.Storable;

public abstract class Test<T extends Storable> {

    public static final class StorageProvider<T extends Storable> {

        public static <T extends Storable> StorageProvider<T> provider(final String name,
            final Function<File, AbstractStorage<T>> builder) {
            return new StorageProvider<>(name, builder);
        }

        public final String name;
        private final Function<File, AbstractStorage<T>> builder;

        private StorageProvider(final String name, final Function<File, AbstractStorage<T>> builder) {
            this.name = name;
            this.builder = builder;
        }

    }

    public final String name;

    public Test(final String name) {
        this.name = name;
    }

    public abstract void createProviders(List<StorageProvider<T>> list);

    public final void executeTest(final File workingDir, final StorageProvider<T> provider, final Profiler profiler, final long seed)
        throws Throwable {
        final AbstractStorage<T> storage = provider.builder.apply(workingDir);
        final Random random = new Random(seed);
        profiler.time();
        executeTest(provider.name, storage, random);
        profiler.time();
        storage.close();
    }

    protected abstract void executeTest(String storageName, AbstractStorage<T> storage, Random random) throws Throwable;

}
