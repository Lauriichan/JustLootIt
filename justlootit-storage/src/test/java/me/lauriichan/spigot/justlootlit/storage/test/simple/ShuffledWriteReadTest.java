package me.lauriichan.spigot.justlootlit.storage.test.simple;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.spigot.justlootit.storage.Storage;
import me.lauriichan.spigot.justlootit.storage.StorageAdapterRegistry;
import me.lauriichan.spigot.justlootit.storage.Stored;
import me.lauriichan.spigot.justlootlit.storage.test.BaseTest;
import me.lauriichan.spigot.justlootlit.storage.test.simple.model.SimpleObject;
import me.lauriichan.spigot.justlootlit.storage.test.simple.model.SimpleObjectAdapter;

public class ShuffledWriteReadTest extends BaseTest {

    private final int amount;

    public ShuffledWriteReadTest(final int amount) {
        super("ShuffledWriteRead (" + Math.abs(amount) + "x)");
        this.amount = Math.abs(amount);
    }

    @Override
    protected void executeTest(final String storageName, final Storage storage, final Random random) {
        if (amount == 0) {
            return;
        }
        final ObjectArrayList<SimpleObject> objects = new ObjectArrayList<>(amount);
        for (int id = 0; id < amount; id++) {
            objects.add(new SimpleObject(random.nextInt(Integer.MAX_VALUE)));
        }

        final ObjectArrayList<SimpleObject> copy = new ObjectArrayList<>(objects);
        Collections.shuffle(copy, random);
        for (SimpleObject object : copy) {
            Stored<SimpleObject> stored = storage.registry().create(object);
            stored.id(objects.indexOf(object));
            storage.write(stored);
        }

        for (int id = 0; id < amount; id++) {
            final Stored<SimpleObject> loaded = storage.read(id);
            assertArrayEquals(objects.get(id).numbers, loaded.value().numbers, "Invalid entry " + id);
        }
    }

    @Override
    protected void setup(final StorageAdapterRegistry storage) {
        storage.register(SimpleObjectAdapter.INSTANCE);
    }

}
