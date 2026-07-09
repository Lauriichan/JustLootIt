package me.lauriichan.spigot.justlootlit.storage.test.simple;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Random;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import me.lauriichan.spigot.justlootit.storage.Storage;
import me.lauriichan.spigot.justlootit.storage.StorageAdapterRegistry;
import me.lauriichan.spigot.justlootit.storage.Stored;
import me.lauriichan.spigot.justlootit.storage.UpdateInfo;
import me.lauriichan.spigot.justlootit.storage.util.counter.CounterProgress;
import me.lauriichan.spigot.justlootlit.storage.test.BaseTest;
import me.lauriichan.spigot.justlootlit.storage.test.simple.model.SimpleObject;
import me.lauriichan.spigot.justlootlit.storage.test.simple.model.SimpleObjectAdapter;

public class WriteRandomizedUpdateReadTest extends BaseTest {

    private final int amount;
    private final int variance;

    public WriteRandomizedUpdateReadTest(final int amount, final int variance) {
        super("WriteRandomizedUpdateRead (" + Math.abs(amount) + "x, 1-" + variance + ")");
        if (variance < 2) {
            throw new IllegalArgumentException("Variance has to be 2 or higher.");
        }
        this.amount = Math.abs(amount);
        this.variance = Math.abs(variance) - 1;
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
            int[] numbers = new int[random.nextInt(variance) + 1];
            for (int i = 0; i < numbers.length; i++) {
                numbers[i] = random.nextInt(Integer.MAX_VALUE);
            }
            final SimpleObject object = new SimpleObject(numbers);
            objects[id] = object;
            storage.write(storage.registry().create(object).id(id));
        }

        final Long2ObjectArrayMap<SimpleObject> objMap = new Long2ObjectArrayMap<>();
        CounterProgress progress = storage.updateEach(stored -> {
            final long mod = stored.id() % 3;
            if (mod == 0) {
                return UpdateInfo.none();
            }
            if (mod == 1) {
                int[] numbers = new int[random.nextInt(variance) + 1];
                for (int i = 0; i < numbers.length; i++) {
                    numbers[i] = random.nextInt(Integer.MAX_VALUE);
                }
                SimpleObject newObj = new SimpleObject(numbers);
                stored.value(newObj);
                objMap.put(stored.id(), newObj);
                return UpdateInfo.modify(stored);
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
            final long mod = id % 3;
            if (mod == 2) {
                assertNull(loaded, "Invalid entry " + id);
                continue;
            }
            if (mod == 1) {
                assertArrayEquals(objMap.get(id).numbers, loaded.value().numbers, "Invalid entry " + id);
                continue;
            }
            assertArrayEquals(objects[id].numbers, loaded.value().numbers, "Invalid entry " + id);
        }
    }

    private void assertArrayEquals(int[] expected, int[] value, String message) {
        StringBuilder assertion = new StringBuilder();
        if (message != null) {
            assertion.append(message).append(" ==> ");
        }
        int maxLength = Math.min(expected.length, value.length);
        for (int i = 0; i < maxLength; i++) {
            if (expected[i] != value[i]) {
                fail(assertion
                    .append("array value at index %s is different, expected: <%s> but was: <%s>".formatted(i, expected[i], value[i]))
                    .toString());
            }
        }
        if (expected.length != value.length) {
            fail(
                assertion.append("array lengths differ, expected: <%s> but was: <%s>".formatted(expected.length, value.length)).toString());
        }
    }

    @Override
    protected void setup(final StorageAdapterRegistry registry) {
        registry.register(SimpleObjectAdapter.INSTANCE);
    }

}
