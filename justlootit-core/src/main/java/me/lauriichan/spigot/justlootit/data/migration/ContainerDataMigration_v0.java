package me.lauriichan.spigot.justlootit.data.migration;

import java.time.OffsetDateTime;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import me.lauriichan.spigot.justlootit.data.Container;
import me.lauriichan.spigot.justlootit.data.io.BufIO;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.data.migration.provider.StorageMigrationExtension;
import me.lauriichan.spigot.justlootit.storage.StorageMigrator;

public abstract class ContainerDataMigration_v0<T extends Container> extends StorageMigrationExtension<T> {

    public ContainerDataMigration_v0(Class<T> targetType) {
        super(targetType, -1, 0);
    }
    
    @Override
    public String description() {
        return "Add access count to container data";
    }
    
    @Override
    public void migrate(StorageMigrator migrator, ByteBuf inBuffer, ByteBuf outBuffer) throws Throwable {
        int amount = inBuffer.readInt();
        outBuffer.writeInt(amount);
        for (int index = 0; index < amount; index++) {
            final UUID uuid = DataIO.UUID.deserialize(inBuffer).value();
            final OffsetDateTime time = DataIO.OFFSET_DATE_TIME.deserialize(inBuffer).value();
            DataIO.UUID.serialize(outBuffer, uuid);
            DataIO.OFFSET_DATE_TIME.serialize(outBuffer, time);
            outBuffer.writeInt(1); // <-- Add first time access
        }
        String refreshGroupId = BufIO.readString(inBuffer);
        BufIO.writeString(outBuffer, refreshGroupId);
        int restBytes = inBuffer.readableBytes();
        if (restBytes != 0) {
            outBuffer.writeBytes(inBuffer, inBuffer.readerIndex(), restBytes);
        }
    }

}
