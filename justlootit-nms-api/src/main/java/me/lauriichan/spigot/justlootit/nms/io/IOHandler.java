package me.lauriichan.spigot.justlootit.nms.io;

import java.lang.reflect.Array;

import io.netty.buffer.ByteBuf;

public abstract class IOHandler<E> {

    public static record Result<T>(T value, boolean dirty) {}

    public static <T> Result<T> result(T value) {
        return new Result<>(value, false);
    }

    public static <T> Result<T> result(T value, boolean dirty) {
        return new Result<>(value, dirty);
    }

    protected final Class<E> type;

    public IOHandler(final Class<E> type) {
        this.type = type;
    }

    public final Class<E> type() {
        return type;
    }

    @SuppressWarnings("unchecked")
    public Result<E[]> deserializeArray(final ByteBuf buffer) {
        final int amount = buffer.readInt();
        final E[] array = (E[]) Array.newInstance(type, amount);
        boolean dirty = false;
        for (int index = 0; index < amount; index++) {
            Result<E> result = deserialize(buffer);
            if (result.dirty()) {
                dirty = true;
            }
            array[index] = result.value();
        }
        return new Result<>(array, dirty);
    }

    public void serializeArray(final ByteBuf buffer, final E[] array) {
        buffer.writeInt(array.length);
        for (int index = 0; index < array.length; index++) {
            serialize(buffer, array[index]);
        }
    }

    public abstract void serialize(ByteBuf buffer, E value);

    public abstract Result<E> deserialize(ByteBuf buffer);

}
