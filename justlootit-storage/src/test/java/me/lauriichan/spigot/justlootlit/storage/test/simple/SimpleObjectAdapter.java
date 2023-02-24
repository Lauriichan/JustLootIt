package me.lauriichan.spigot.justlootlit.storage.test.simple;

import io.netty.buffer.ByteBuf;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;

public class SimpleObjectAdapter  extends StorageAdapter<SimpleObject> {

    public static final SimpleObjectAdapter INSTANCE = new SimpleObjectAdapter();

    private SimpleObjectAdapter() {
        super(SimpleObject.class);
    }

    @Override
    public void serialize(SimpleObject storable, ByteBuf buffer) {
        
    }

    @Override
    public SimpleObject deserialize(long id, ByteBuf buffer) {
        // TODO Auto-generated method stub
        return null;
    }

}