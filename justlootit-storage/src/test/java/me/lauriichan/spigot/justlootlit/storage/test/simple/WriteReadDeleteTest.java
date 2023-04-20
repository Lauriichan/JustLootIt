package me.lauriichan.spigot.justlootlit.storage.test.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import me.lauriichan.spigot.justlootit.storage.AbstractStorage;
import me.lauriichan.spigot.justlootlit.storage.test.BaseTest;
import me.lauriichan.spigot.justlootlit.storage.test.simple.model.SimpleObject;
import me.lauriichan.spigot.justlootlit.storage.test.simple.model.SimpleObjectAdapter;

public class WriteReadDeleteTest extends BaseTest<SimpleObject> {

    private final int amount;

    public WriteReadDeleteTest(final int amount) {
        super("WriteReadDelete (" + Math.abs(amount) + "x)", SimpleObject.class);
        this.amount = Math.abs(amount);
    }

    @Override
    protected void executeTest(final String storageName, final AbstractStorage<SimpleObject> storage, final Random random) {
        if (amount == 0) {
            return;
        }
        final SimpleObject[] objects = new SimpleObject[amount];
        for (int id = 0; id < amount; id++) {
            final SimpleObject object = new SimpleObject(id, random.nextInt(Integer.MAX_VALUE));
            objects[id] = object;
            storage.write(object);
        }

        for (int id = 0; id < amount; id++) {
            final SimpleObject loaded = storage.read(id);
            assertEquals(objects[id].number, loaded.number, "Invalid entry " + id);
            if (id % 2 == 0) {
                assertTrue(storage.delete(id), "Invalid entry " + id);
            }
        }
    }

    @Override
    protected void setup(final AbstractStorage<SimpleObject> storage) {
        storage.register(SimpleObjectAdapter.INSTANCE);
    }

}
