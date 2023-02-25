package me.lauriichan.spigot.justlootlit.storage.test.simple;

//import static org.junit.jupiter.api.Assertions.assertEquals;

import me.lauriichan.spigot.justlootit.storage.Storage;
import me.lauriichan.spigot.justlootlit.storage.test.BaseTest;

public class SimpleTest extends BaseTest<SimpleObject> {

    private final int amount;
    
    public SimpleTest(int amount) {
        super("Simple (" + Math.abs(amount) + ")", SimpleObject.class);
        this.amount = Math.abs(amount);
    }

    @Override
    protected void executeTest(String storageName, Storage<SimpleObject> storage) {
        if(amount == 0) {
            return;
        }
        SimpleObject[] objects = new SimpleObject[amount];
        for(int id = 0; id < amount; id++) {
            SimpleObject object = new SimpleObject(id);
            objects[id] = object;
            storage.write(object);
        }
    }

    @Override
    protected void setup(Storage<SimpleObject> storage) {
        storage.register(SimpleObjectAdapter.INSTANCE);
    }

}
