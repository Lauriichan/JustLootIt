package me.lauriichan.spigot.justlootit.storage;

import java.util.Objects;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public abstract class StorageAdapter<T extends Storable> {

    protected final Class<T> type;

    public StorageAdapter(final Class<T> type) {
        this.type = Objects.requireNonNull(type);
    }

    public final Class<T> type() {
        return type;
    }
    
    public final ByteBuf serializeValue(Storable storable) {
        return serialize(type.cast(storable));
    }
    
    public final void serializeValue(Storable storable, ByteBuf buffer) {
        serialize(type.cast(storable), buffer);
    }

    public final ByteBuf serialize(T storable) {
        ByteBuf buffer = Unpooled.buffer();
        serialize(storable, buffer);
        return buffer.asReadOnly();
    }

    public abstract void serialize(T storable, ByteBuf buffer);

    public abstract T deserialize(long id, ByteBuf buffer);

}
