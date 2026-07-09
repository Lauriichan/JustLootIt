package me.lauriichan.spigot.justlootlit.storage.test.simple;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Random;

import me.lauriichan.spigot.justlootit.storage.Storage;
import me.lauriichan.spigot.justlootit.storage.StorageAdapterRegistry;
import me.lauriichan.spigot.justlootit.storage.Stored;
import me.lauriichan.spigot.justlootit.storage.UpdateInfo;
import me.lauriichan.spigot.justlootit.storage.util.counter.CounterProgress;
import me.lauriichan.spigot.justlootlit.storage.test.BaseTest;
import me.lauriichan.spigot.justlootlit.storage.test.simple.model.SimpleObject;
import me.lauriichan.spigot.justlootlit.storage.test.simple.model.SimpleObjectAdapter;

public class WriteMassDeleteReadTest extends BaseTest {

    private final int amount;

    public WriteMassDeleteReadTest(final int amount) {
        super("WriteMassDeleteRead (" + Math.abs(amount) + "x)");
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
            storage.write(storage.registry().create(object).id(id));
        }

        CounterProgress progress = storage.updateEach(stored -> {
            final long mod = stored.id() % 2;
            if (mod == 0) {
                return UpdateInfo.none();
            }
            return UpdateInfo.delete();
        }, Runnable::run);

        if (!progress.hasFutures()) {
            fail("No futures available");
            return;
        }

        while (!progress.isDone()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
        }

        for (int id = 0; id < actualAmount; id++) {
            final Stored<SimpleObject> loaded = storage.read(id);
            final long mod = id % 2;
            if (mod == 1) {
                assertNull(loaded, "Invalid entry " + id);
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
