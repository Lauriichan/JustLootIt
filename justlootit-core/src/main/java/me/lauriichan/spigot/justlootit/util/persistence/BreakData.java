package me.lauriichan.spigot.justlootit.util.persistence;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.bukkit.persistence.PersistentDataAdapterContext;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.util.SimpleDataType;

public record BreakData(UUID playerId, OffsetDateTime time) {
    
    public static final SimpleDataType<byte[], BreakData> BREAK_DATA_TYPE = new SimpleDataType<>(byte[].class, BreakData.class) {
        @Override
        public byte[] toPrimitive(BreakData complex, PersistentDataAdapterContext context) {
            ByteBuf buffer = Unpooled.buffer();
            DataIO.UUID.serialize(buffer, complex.playerId());
            DataIO.OFFSET_DATE_TIME.serialize(buffer, complex.time());
            return buffer.array();
        }
        @Override
        public BreakData fromPrimitive(byte[] primitive, PersistentDataAdapterContext context) {
            ByteBuf buffer = Unpooled.wrappedBuffer(primitive);
            UUID playerId = DataIO.UUID.deserialize(buffer);
            OffsetDateTime time = DataIO.OFFSET_DATE_TIME.deserialize(buffer);
            return new BreakData(playerId, time);
        }
    };

}
