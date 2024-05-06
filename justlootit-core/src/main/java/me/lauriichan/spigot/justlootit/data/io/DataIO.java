package me.lauriichan.spigot.justlootit.data.io;

import static me.lauriichan.spigot.justlootit.data.io.BufIO.readString;
import static me.lauriichan.spigot.justlootit.data.io.BufIO.writeString;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.bukkit.NamespacedKey;

import io.netty.buffer.ByteBuf;
import me.lauriichan.spigot.justlootit.nms.io.IOHandler;
import me.lauriichan.spigot.justlootit.nms.io.IOProvider;

public final class DataIO {

    private DataIO() {
        throw new UnsupportedOperationException("Data IO class");
    }

    public static final IOHandler<NamespacedKey> NAMESPACED_KEY = new IOHandler<>(NamespacedKey.class) {
        @Override
        public void serialize(final ByteBuf buffer, final NamespacedKey value) {
            writeString(buffer, value.getNamespace());
            writeString(buffer, value.getKey());
        }

        @Override
        public Result<NamespacedKey> deserialize(final ByteBuf buffer) {
            final String namespace = readString(buffer);
            final String key = readString(buffer);
            // Use fromString to prevent deprecation
            return result(NamespacedKey.fromString(namespace + ':' + key));
        }
    };

    public static final IOHandler<java.util.UUID> UUID = new IOHandler<>(java.util.UUID.class) {
        @Override
        public void serialize(final ByteBuf buffer, final java.util.UUID value) {
            buffer.writeLong(value.getMostSignificantBits());
            buffer.writeLong(value.getLeastSignificantBits());
        }

        @Override
        public Result<java.util.UUID> deserialize(final ByteBuf buffer) {
            final long most = buffer.readLong();
            final long least = buffer.readLong();
            return result(new java.util.UUID(most, least));
        }
    };

    public static final IOHandler<OffsetDateTime> OFFSET_DATE_TIME = new IOHandler<>(OffsetDateTime.class) {
        @Override
        public void serialize(final ByteBuf buffer, final OffsetDateTime value) {
            buffer.writeInt(value.getOffset().getTotalSeconds());
            buffer.writeInt(value.getYear());
            buffer.writeByte(value.getMonthValue());
            buffer.writeByte(value.getDayOfMonth());
            buffer.writeByte(value.getHour());
            buffer.writeByte(value.getMinute());
            buffer.writeByte(value.getSecond());
            buffer.writeInt(value.getNano());
        }

        @Override
        public Result<OffsetDateTime> deserialize(final ByteBuf buffer) {
            final ZoneOffset offset = ZoneOffset.ofTotalSeconds(buffer.readInt());
            final int year = buffer.readInt();
            final int month = buffer.readByte();
            final int dayOfMonth = buffer.readByte();
            final int hour = buffer.readByte();
            final int minute = buffer.readByte();
            final int second = buffer.readByte();
            final int nanoOfSecond = buffer.readInt();
            return result(OffsetDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond, offset));
        }
    };

    private static IOProvider IO;

    public static void setup(final IOProvider io) {
        if (IO != null || io == null) {
            return;
        }
        IO = io;
        register();
    }

    private static void register() {
        IO.register(NAMESPACED_KEY);
        IO.register(OFFSET_DATE_TIME);
        IO.register(UUID);
    }

    @SuppressWarnings("unchecked")
    public static <T> IOHandler<T> find(final Class<T> type) {
        final IOHandler<?> handler = IO.handlerOf(type);
        if (handler == null) {
            return null;
        }
        return (IOHandler<T>) handler;
    }

}
