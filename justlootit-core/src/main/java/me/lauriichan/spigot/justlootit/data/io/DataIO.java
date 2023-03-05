package me.lauriichan.spigot.justlootit.data.io;

import static me.lauriichan.spigot.justlootit.data.io.BufIO.*;

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
        public void serialize(ByteBuf buffer, NamespacedKey value) {
            writeString(buffer, value.getNamespace());
            writeString(buffer, value.getKey());
        }

        @Override
        public NamespacedKey deserialize(ByteBuf buffer) {
            String namespace = readString(buffer);
            String key = readString(buffer);
            // Use fromString to prevent deprecation
            return NamespacedKey.fromString(namespace + ':' + key);
        }
    };

    public static final IOHandler<java.util.UUID> UUID = new IOHandler<>(java.util.UUID.class) {
        @Override
        public void serialize(ByteBuf buffer, java.util.UUID value) {
            buffer.writeLong(value.getMostSignificantBits());
            buffer.writeLong(value.getLeastSignificantBits());
        }

        @Override
        public java.util.UUID deserialize(ByteBuf buffer) {
            long most = buffer.readLong();
            long least = buffer.readLong();
            return new java.util.UUID(most, least);
        }
    };

    public static final IOHandler<OffsetDateTime> OFFSET_DATE_TIME = new IOHandler<>(OffsetDateTime.class) {
        @Override
        public void serialize(ByteBuf buffer, OffsetDateTime value) {
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
        public OffsetDateTime deserialize(ByteBuf buffer) {
            ZoneOffset offset = ZoneOffset.ofTotalSeconds(buffer.readInt());
            int year = buffer.readInt();
            int month = buffer.readByte();
            int dayOfMonth = buffer.readByte();
            int hour = buffer.readByte();
            int minute = buffer.readByte();
            int second = buffer.readByte();
            int nanoOfSecond = buffer.readInt();
            return OffsetDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond, offset);
        }
    };

    private static IOProvider IO;

    public static void setup(IOProvider io) {
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
    public static <T> IOHandler<T> find(Class<T> type) {
        IOHandler<?> handler = IO.handlerOf(type);
        if (handler == null) {
            return null;
        }
        return (IOHandler<T>) handler;
    }

}
