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

    public WriteUpdateReadTest(final int amount) {
        super("WriteUpdateRead (" + Math.abs(amount) + "x)", SimpleObject.class);
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

        storage.updateEach(object -> {
            final long mod = object.id() % 3;
            if (mod == 0) {
                return UpdateInfo.none();
            }
            if (mod == 1) {
                return UpdateInfo.modify(object.withNumber(random.nextInt(Integer.MAX_VALUE)));
            }
            return UpdateInfo.delete();
        });

        for (int id = 0; id < amount; id++) {
            final SimpleObject loaded = storage.read(id);
            final long mod = id % 3;
            if (mod == 2) {
                assertNull(loaded, "Invalid entry " + id);
                continue;
            }
            if (mod == 0) {
                assertEquals(objects[id].number, loaded.number, "Invalid entry " + id);
                continue;
            }
            assertNotEquals(objects[id].number, loaded.number, "Invalid entry " + id);
        }
    }

    @Override
    protected void setup(final AbstractStorage<SimpleObject> storage) {
        storage.register(SimpleObjectAdapter.INSTANCE);
    }

}
