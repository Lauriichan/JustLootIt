package me.lauriichan.spigot.justlootlit.storage.test.simple.model;

import io.netty.buffer.ByteBuf;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;
import me.lauriichan.spigot.justlootit.storage.StorageAdapterRegistry;

public class SimpleObjectAdapter extends StorageAdapter<SimpleObject> {

    public static final SimpleObjectAdapter INSTANCE = new SimpleObjectAdapter();

    private SimpleObjectAdapter() {
        super(SimpleObject.class, 0);
    }

    @Override
    public void serialize(StorageAdapterRegistry registry, SimpleObject value, ByteBuf buffer) {
        buffer.writeInt(value.numbers.length);
        for (int index = 0; index < value.numbers.length; index++) {
            buffer.writeInt(value.numbers[index]);
        }
    }

    @Override
    public SimpleObject deserialize(StorageAdapterRegistry registry, ByteBuf buffer) {
        int[] numbers = new int[buffer.readInt()];
        for (int index = 0; index < numbers.length; index++) {
            numbers[index] = buffer.readInt();
        }
        return new SimpleObject(numbers);
    }

}