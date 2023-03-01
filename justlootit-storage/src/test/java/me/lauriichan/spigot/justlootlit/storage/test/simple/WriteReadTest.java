package me.lauriichan.spigot.justlootlit.storage.test.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import me.lauriichan.spigot.justlootit.storage.Storage;
import me.lauriichan.spigot.justlootlit.storage.test.BaseTest;
import me.lauriichan.spigot.justlootlit.storage.test.simple.model.SimpleObject;
import me.lauriichan.spigot.justlootlit.storage.test.simple.model.SimpleObjectAdapter;

public class WriteReadTest extends BaseTest<SimpleObject> {

    private final int amount;
    
    public WriteReadTest(int amount) {
        super("WriteRead (" + Math.abs(amount) + "x)", SimpleObject.class);
        this.amount = Math.abs(amount);
    }

    @Override
    protected void executeTest(String storageName, Storage<SimpleObject> storage, Random random) {
        if(amount == 0) {
            return;
        }
        SimpleObject[] objects = new SimpleObject[amount];
        for(int id = 0; id < amount; id++) {
            SimpleObject object = new SimpleObject(id, random.nextInt(Integer.MAX_VALUE));
            objects[id] = object;
            storage.write(object);
        }
        
        for(int id = 0; id < amount; id++) {
            SimpleObject loaded = storage.read(id);
            assertEquals(objects[id].number, loaded.number, "Invalid entry " + id);
        }
    }

    @Override
    protected void setup(Storage<SimpleObject> storage) {
        storage.register(SimpleObjectAdapter.INSTANCE);
    }

}
