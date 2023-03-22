package me.lauriichan.spigot.justlootlit.storage.test.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Random;

import me.lauriichan.spigot.justlootit.storage.AbstractStorage;
import me.lauriichan.spigot.justlootit.storage.UpdateInfo;
import me.lauriichan.spigot.justlootlit.storage.test.BaseTest;
import me.lauriichan.spigot.justlootlit.storage.test.simple.model.SimpleObject;
import me.lauriichan.spigot.justlootlit.storage.test.simple.model.SimpleObjectAdapter;

public class WriteUpdateReadTest extends BaseTest<SimpleObject> {

    private final int amount;
    
    public WriteUpdateReadTest(int amount) {
        super("WriteUpdateRead (" + Math.abs(amount) + "x)", SimpleObject.class);
        this.amount = Math.abs(amount);
    }

    @Override
    protected void executeTest(String storageName, AbstractStorage<SimpleObject> storage, Random random) {
        if(amount == 0) {
            return;
        }
        SimpleObject[] objects = new SimpleObject[amount];
        for(int id = 0; id < amount; id++) {
            SimpleObject object = new SimpleObject(id, random.nextInt(Integer.MAX_VALUE));
            objects[id] = object;
            storage.write(object);
        }
        
        storage.updateEach(object -> {
            long mod = object.id() % 3;
            if(mod == 0) {
                return UpdateInfo.none();
            }
            if(mod == 1) {
                return UpdateInfo.modify(object.withNumber(random.nextInt(Integer.MAX_VALUE)));
            }
            return UpdateInfo.delete();
        });

        for(int id = 0; id < amount; id++) {
            SimpleObject loaded = storage.read(id);
            long mod = id % 3;
            if(mod == 2) {
                assertNull(loaded, "Invalid entry " + id);
                continue;
            }
            if(mod == 0) {
                assertEquals(objects[id].number, loaded.number, "Invalid entry " + id);
                continue;
            }
            assertNotEquals(objects[id].number, loaded.number, "Invalid entry " + id);
        }
    }

    @Override
    protected void setup(AbstractStorage<SimpleObject> storage) {
        storage.register(SimpleObjectAdapter.INSTANCE);
    }

}
