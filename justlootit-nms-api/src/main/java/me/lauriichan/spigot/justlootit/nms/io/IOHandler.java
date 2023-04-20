package me.lauriichan.spigot.justlootit.nms.io;

import java.lang.reflect.Array;

import io.netty.buffer.ByteBuf;

public abstract class IOHandler<E> {

    protected final Class<E> type;

    public IOHandler(final Class<E> type) {
        this.type = type;
    }

    public final Class<E> type() {
        return type;
    }

    @SuppressWarnings("unchecked")
    public E[] deserializeArray(final ByteBuf buffer) {
        final int amount = buffer.readInt();
        final E[] array = (E[]) Array.newInstance(type, amount);
        for (int index = 0; index < amount; index++) {
            array[index] = deserialize(buffer);
        }
        return array;
    }

    public void serializeArray(final ByteBuf buffer, final E[] array) {
        buffer.writeInt(array.length);
        for (int index = 0; index < array.length; index++) {
            serialize(buffer, array[index]);
        }
    }

    public abstract void serialize(ByteBuf buffer, E value);

    public abstract E deserialize(ByteBuf buffer);

}
