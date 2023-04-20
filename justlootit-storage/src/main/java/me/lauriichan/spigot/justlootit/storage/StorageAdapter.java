package me.lauriichan.spigot.justlootit.storage;

import java.util.Objects;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public abstract class StorageAdapter<T extends Storable> {

    protected final Class<T> type;
    protected final short typeId;

    public StorageAdapter(final Class<T> type, final int typeId) {
        this.type = Objects.requireNonNull(type);
        this.typeId = (short) (typeId & 0xFF);
    }

    public final Class<T> type() {
        return type;
    }

    public final short typeId() {
        return typeId;
    }

    public final ByteBuf serializeValue(final Storable storable) {
        return serialize(type.cast(storable));
    }

    public final void serializeValue(final Storable storable, final ByteBuf buffer) {
        serialize(type.cast(storable), buffer);
    }

    public final ByteBuf serialize(final T storable) {
        final ByteBuf buffer = Unpooled.buffer();
        serialize(storable, buffer);
        return buffer;
    }

    public abstract void serialize(T storable, ByteBuf buffer);

    public abstract T deserialize(long id, ByteBuf buffer);

}
