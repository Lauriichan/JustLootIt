package me.lauriichan.spigot.justlootlit.storage.test;

import static me.lauriichan.spigot.justlootlit.storage.test.Test.StorageProvider.provider;

import java.util.List;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.storage.Storage;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.RAFStorage;
import me.lauriichan.spigot.justlootit.storage.util.SystemSimpleLogger;

public abstract class BaseTest<T extends Storable> extends Test<T> {

    private static final ISimpleLogger SYSOUT_LOGGER = SystemSimpleLogger.SYSTEM;

    public final Class<T> type;

    public BaseTest(final String name, final Class<T> type) {
        super(name);
        this.type = type;
    }

    @Override
    public void createProviders(List<StorageProvider<T>> list) {
        list.add(provider("RandomAccessFile", (file) -> apply(new RAFStorage<>(SYSOUT_LOGGER, type, file))));
    }

    private Storage<T> apply(Storage<T> storage) {
        setup(storage);
        return storage;
    }

    protected abstract void setup(Storage<T> storage);

}
