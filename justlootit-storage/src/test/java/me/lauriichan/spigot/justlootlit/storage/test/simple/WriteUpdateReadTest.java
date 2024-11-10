package me.lauriichan.spigot.justlootlit.storage.test.simple;

import static me.lauriichan.spigot.justlootlit.storage.test.junit.AssertArrayNotEquals.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Random;

import me.lauriichan.spigot.justlootit.storage.Storage;
import me.lauriichan.spigot.justlootit.storage.StorageAdapterRegistry;
import me.lauriichan.spigot.justlootit.storage.Stored;
import me.lauriichan.spigot.justlootit.storage.UpdateInfo;
import me.lauriichan.spigot.justlootlit.storage.test.BaseTest;
import me.lauriichan.spigot.justlootlit.storage.test.simple.model.SimpleObject;
import me.lauriichan.spigot.justlootlit.storage.test.simple.model.SimpleObjectAdapter;

public class WriteUpdateReadTest extends BaseTest {

    private final int amount;

    public WriteUpdateReadTest(final int amount) {
        super("WriteUpdateRead (" + Math.abs(amount) + "x)");
        this.amount = Math.abs(amount);
    }

    @Override
    protected void executeTest(final String storageName, final Storage storage, final Random random) {
        if (amount == 0) {
            return;
        }
        final SimpleObject[] objects = new SimpleObject[amount];
        for (int id = 0; id < amount; id++) {
            final SimpleObject object = new SimpleObject(random.nextInt(Integer.MAX_VALUE));
            objects[id] = object;
            storage.write(storage.registry().create(object).id(id));
        }

        storage.updateEach(stored -> {
            final long mod = stored.id() % 3;
            if (mod == 0) {
                return UpdateInfo.none();
            }
            if (mod == 1) {
                stored.value(new SimpleObject(random.nextInt(Integer.MAX_VALUE)));
                return UpdateInfo.modify(stored);
            }
            return UpdateInfo.delete();
        }, Runnable::run);

        for (int id = 0; id < amount; id++) {
            final Stored<SimpleObject> loaded = storage.read(id);
            final long mod = id % 3;
            if (mod == 2) {
                assertNull(loaded, "Invalid entry " + id);
                continue;
            }
            if (mod == 1) {
                assertArrayNotEquals(objects[id].numbers, loaded.value().numbers, "Invalid entry " + id);
                continue;
            }
            assertArrayEquals(objects[id].numbers, loaded.value().numbers, "Invalid entry " + id);
        }
    }

    @Override
    protected void setup(final StorageAdapterRegistry registry) {
        registry.register(SimpleObjectAdapter.INSTANCE);
    }

}
