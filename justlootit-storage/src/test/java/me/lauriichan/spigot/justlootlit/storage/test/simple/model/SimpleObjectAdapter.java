package me.lauriichan.spigot.justlootlit.storage.test.simple.model;

import io.netty.buffer.ByteBuf;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;

public class SimpleObjectAdapter extends StorageAdapter<SimpleObject> {

    public static final SimpleObjectAdapter INSTANCE = new SimpleObjectAdapter();

    private SimpleObjectAdapter() {
        super(SimpleObject.class, 0);
    }

    @Override
    public void serialize(final SimpleObject storable, final ByteBuf buffer) {
        buffer.writeInt(storable.numbers.length);
        for(int index = 0; index < storable.numbers.length; index++) {
            buffer.writeInt(storable.numbers[index]);
        }
    }

    @Override
    public SimpleObject deserialize(final long id, final ByteBuf buffer) {
        int[] numbers = new int[buffer.readInt()];
        for(int index = 0; index < numbers.length; index++) {
            numbers[index] = buffer.readInt();
        }
        return new SimpleObject(id, numbers);
    }

}