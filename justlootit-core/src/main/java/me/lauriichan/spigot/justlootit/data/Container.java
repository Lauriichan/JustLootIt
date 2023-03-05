package me.lauriichan.spigot.justlootit.data;

import java.time.OffsetDateTime;
import java.util.Map.Entry;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;

public abstract class Container extends Storable {

    protected static abstract class BaseAdapter<C extends Container> extends StorageAdapter<C> {

        public BaseAdapter(Class<C> type, int typeId) {
            super(type, typeId);
        }

        @Override
        public final void serialize(C storable, ByteBuf buffer) {
            ContainerData data = storable.containerData;
            buffer.writeInt(data.playerAccess.size());
            for(Entry<UUID, OffsetDateTime> entry : data.playerAccess.entrySet()) {
                DataIO.UUID.serialize(buffer, entry.getKey());
                DataIO.OFFSET_DATE_TIME.serialize(buffer, entry.getValue());
            }
            DataIO.OFFSET_DATE_TIME.serialize(buffer, data.refreshDate);
            serializeSpecial(storable, buffer);
        }

        @Override
        public final C deserialize(long id, ByteBuf buffer) {
            ContainerData data = new ContainerData();
            int amount = buffer.readInt();
            for (int index = 0; index < amount; index++) {
                UUID uuid = DataIO.UUID.deserialize(buffer);
                OffsetDateTime time = DataIO.OFFSET_DATE_TIME.deserialize(buffer);
                data.playerAccess.put(uuid, time);
            }
            data.refreshDate = DataIO.OFFSET_DATE_TIME.deserialize(buffer);
            return deserializeSpecial(id, data, buffer);
        }

        protected abstract void serializeSpecial(C storable, ByteBuf buffer);

        protected abstract C deserializeSpecial(long id, ContainerData data, ByteBuf buffer);

    }

    protected static final class ContainerData {

        private final Object2ObjectOpenHashMap<UUID, OffsetDateTime> playerAccess = new Object2ObjectOpenHashMap<>();
        private OffsetDateTime refreshDate;

    }

    final ContainerData containerData;

    public Container(long id) {
        this(id, new ContainerData());
        containerData.refreshDate = OffsetDateTime.now();
    }

    public Container(long id, ContainerData containerData) {
        super(id);
        this.containerData = containerData;
    }

}
