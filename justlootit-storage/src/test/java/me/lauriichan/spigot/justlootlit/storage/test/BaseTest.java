package me.lauriichan.spigot.justlootlit.storage.test;

import static me.lauriichan.spigot.justlootlit.storage.test.Test.StorageProvider.provider;

import java.util.List;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.storage.AbstractStorage;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.RAFMultiStorage;
import me.lauriichan.spigot.justlootit.storage.util.SystemSimpleLogger;

public abstract class BaseTest<T extends Storable> extends Test<T> {

    private static final ISimpleLogger SYSOUT_LOGGER = SystemSimpleLogger.SYSTEM;

    public final Class<T> type;

    public BaseTest(final String name, final Class<T> type) {
        super(name);
        this.type = type;
    }

    @Override
    public void createProviders(final List<StorageProvider<T>> list) {
        list.add(provider("RandomAccessFile", file -> apply(new RAFMultiStorage<>(SYSOUT_LOGGER, type, file))));
    }

    private AbstractStorage<T> apply(final AbstractStorage<T> storage) {
        setup(storage);
        return storage;
    }

    protected abstract void setup(AbstractStorage<T> storage);

}
