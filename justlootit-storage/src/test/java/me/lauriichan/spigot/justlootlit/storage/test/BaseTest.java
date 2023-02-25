package me.lauriichan.spigot.justlootlit.storage.test;

import static me.lauriichan.spigot.justlootlit.storage.test.Test.StorageProvider.provider;

import java.util.List;
import java.util.logging.Logger;

import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.RAFStorage;

public abstract class BaseTest<T extends Storable> extends Test<T> {

    private static final Logger LOGGER = Logger.getLogger("TestLogger");

    public final Class<T> type;

    public BaseTest(final String name, final Class<T> type) {
        super(name);
        this.type = type;
    }

    @Override
    public void createProviders(List<StorageProvider<T>> list) {
        list.add(provider("RandomAccessFile", (file) -> new RAFStorage<>(LOGGER, type, file)));
    }

}
