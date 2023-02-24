package me.lauriichan.spigot.justlootlit.storage.test;

import java.util.List;

import me.lauriichan.spigot.justlootit.storage.Storable;

public abstract class BaseTest<T extends Storable> extends Test<T> {

    public final Class<T> type;

    public BaseTest(final String name, final Class<T> type) {
        super(name);
        this.type = type;
    }

    @Override
    public void createProviders(List<StorageProvider<T>> list) {
        
    }

}
