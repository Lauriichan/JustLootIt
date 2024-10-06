package me.lauriichan.spigot.justlootlit.storage.test;

import static me.lauriichan.spigot.justlootlit.storage.test.Test.StorageProvider.provider;

import java.util.List;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.storage.StorageAdapterRegistry;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.RAFMultiStorage;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.RAFSingleStorage;
import me.lauriichan.spigot.justlootit.storage.util.SystemSimpleLogger;
import me.lauriichan.spigot.justlootlit.storage.test.simple.model.SimpleStorageMigrator;

public abstract class BaseTest extends Test {

    private static final ISimpleLogger SYSOUT_LOGGER = SystemSimpleLogger.SYSTEM;

    public BaseTest(final String name) {
        super(name);
    }

    @Override
    public void createProviders(final List<StorageProvider> list) {
        StorageAdapterRegistry registry = new StorageAdapterRegistry(new SimpleStorageMigrator(SYSOUT_LOGGER));
        setup(registry);
        list.add(provider("RandomAccessFile (Multi)", file -> new RAFMultiStorage(registry, file)));
        list.add(provider("RandomAccessFile (Single)", file -> new RAFSingleStorage(registry, file)));
    }

    protected abstract void setup(StorageAdapterRegistry registry);

}
