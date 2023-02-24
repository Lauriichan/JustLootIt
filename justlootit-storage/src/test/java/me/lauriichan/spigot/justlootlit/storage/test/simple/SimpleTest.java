package me.lauriichan.spigot.justlootlit.storage.test.simple;

import me.lauriichan.spigot.justlootit.storage.Storage;
import me.lauriichan.spigot.justlootlit.storage.test.BaseTest;

public class SimpleTest extends BaseTest<SimpleObject> {

    public SimpleTest() {
        super("Simple", SimpleObject.class);
    }

    @Override
    protected void executeTest(String storageName, Storage<SimpleObject> storage) {
        
    }

}
