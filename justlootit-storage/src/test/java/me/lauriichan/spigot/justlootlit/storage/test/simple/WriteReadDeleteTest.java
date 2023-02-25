package me.lauriichan.spigot.justlootlit.storage.test.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import me.lauriichan.spigot.justlootit.storage.Storage;
import me.lauriichan.spigot.justlootlit.storage.test.BaseTest;
import me.lauriichan.spigot.justlootlit.storage.test.simple.model.SimpleObject;
import me.lauriichan.spigot.justlootlit.storage.test.simple.model.SimpleObjectAdapter;

public class WriteReadDeleteTest extends BaseTest<SimpleObject> {

    private final int amount;
    
    public WriteReadDeleteTest(int amount) {
        super("WriteReadDelete (" + Math.abs(amount) + "x)", SimpleObject.class);
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
        
        for(int id = 0; id < amount; id++) {
            SimpleObject loaded = storage.read(id);
            assertEquals(loaded.number, objects[id].number);
            
            if(id % 2 == 0) {
                assertTrue(storage.delete(id));
            }
        }
    }

    @Override
    protected void setup(Storage<SimpleObject> storage) {
        storage.register(SimpleObjectAdapter.INSTANCE);
    }

}
