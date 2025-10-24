package me.lauriichan.spigot.justlootlit.storage.test.simple;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Random;

import me.lauriichan.spigot.justlootit.storage.Storage;
import me.lauriichan.spigot.justlootit.storage.StorageAdapterRegistry;
import me.lauriichan.spigot.justlootit.storage.Stored;
import me.lauriichan.spigot.justlootlit.storage.test.BaseTest;
import me.lauriichan.spigot.justlootlit.storage.test.simple.model.SimpleObject;
import me.lauriichan.spigot.justlootlit.storage.test.simple.model.SimpleObjectAdapter;

public class WriteOverwriteReadTest extends BaseTest {

    private final int amount;

    public WriteOverwriteReadTest(final int amount) {
        super("WriteOverwriteRead (" + Math.abs(amount) + "x)");
        this.amount = Math.abs(amount);
    }

    @Override
    protected void executeTest(final String storageName, final Storage storage, final Random random) {
        if (amount == 0) {
            return;
        }
        final SimpleObject[] objects = new SimpleObject[amount];
        int actualAmount = amount;
        for (int id = 0; id < amount; id++) {
            if (!storage.isSupported(id)) {
                actualAmount = id;
                break;
            }
            final SimpleObject object = new SimpleObject(random.nextInt(Integer.MAX_VALUE));
            objects[id] = object;
            Stored<SimpleObject> stored = storage.registry().create(object);
            stored.id(id);
            storage.write(stored);
        }
        
        for(int id = 0; id < actualAmount; id++) {
            SimpleObject object = objects[id];
            objects[id] = (object = new SimpleObject(object.numbers[0], random.nextInt(Integer.MAX_VALUE)));
            Stored<SimpleObject> stored = storage.registry().create(object);
            stored.id(id);
            storage.write(stored);
        }
        
        for (int id = 0; id < actualAmount; id++) {
            final Stored<SimpleObject> loaded = storage.read(id);
            assertArrayEquals(objects[id].numbers, loaded.value().numbers, "Invalid entry " + id);
        }
    }

    @Override
    protected void setup(final StorageAdapterRegistry registry) {
        registry.register(SimpleObjectAdapter.INSTANCE);
    }

}
