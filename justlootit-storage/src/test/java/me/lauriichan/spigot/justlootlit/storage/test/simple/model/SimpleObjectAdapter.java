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
        buffer.writeInt(storable.number);
    }

    @Override
    public SimpleObject deserialize(final long id, final ByteBuf buffer) {
        return new SimpleObject(id, buffer.readInt());
    }

}