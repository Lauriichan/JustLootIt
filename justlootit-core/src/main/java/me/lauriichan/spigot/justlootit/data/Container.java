package me.lauriichan.spigot.justlootit.data;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.storage.IModifiable;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;

public abstract class Container extends Storable implements IModifiable {

    protected static abstract class BaseAdapter<C extends Container> extends StorageAdapter<C> {

        public BaseAdapter(final Class<C> type, final int typeId) {
            super(type, typeId);
        }

        @Override
        public final void serialize(final C storable, final ByteBuf buffer) {
            final ContainerData data = storable.data;
            buffer.writeInt(data.playerAccess.size());
            for (final Entry<UUID, OffsetDateTime> entry : data.playerAccess.entrySet()) {
                DataIO.UUID.serialize(buffer, entry.getKey());
                DataIO.OFFSET_DATE_TIME.serialize(buffer, entry.getValue());
            }
            buffer.writeLong(data.refreshInterval);
            serializeSpecial(storable, buffer);
        }

        @Override
        public final C deserialize(final long id, final ByteBuf buffer) {
            final ContainerData data = new ContainerData();
            final int amount = buffer.readInt();
            for (int index = 0; index < amount; index++) {
                final UUID uuid = DataIO.UUID.deserialize(buffer);
                final OffsetDateTime time = DataIO.OFFSET_DATE_TIME.deserialize(buffer);
                data.playerAccess.put(uuid, time);
            }
            data.refreshInterval = Math.max(buffer.readLong(), 0);
            return deserializeSpecial(id, data, buffer);
        }

        protected abstract void serializeSpecial(C storable, ByteBuf buffer);

        protected abstract C deserializeSpecial(long id, ContainerData data, ByteBuf buffer);

    }

    protected static final class ContainerData {

        private final Object2ObjectOpenHashMap<UUID, OffsetDateTime> playerAccess = new Object2ObjectOpenHashMap<>();
        private long refreshInterval = 0;

    }

    final ContainerData data;
    private boolean dirty = false;

    public Container(final long id) {
        this(id, new ContainerData());
    }

    public Container(final long id, final ContainerData data) {
        super(id);
        this.data = data;
    }

    @Override
    public final boolean isDirty() {
        return dirty;
    }

    protected final void setDirty() {
        this.dirty = true;
    }

    public OffsetDateTime getAccessTime(final UUID id) {
        return data.playerAccess.get(id);
    }

    public Duration durationUntilNextAccess(final UUID id) {
        if (data.refreshInterval == 0) {
            return Duration.ofSeconds(-1);
        }
        final OffsetDateTime time = data.playerAccess.get(id);
        if (time == null) {
            return Duration.ZERO;
        }
        final Duration duration = Duration.between(OffsetDateTime.now(), time.plus(data.refreshInterval, ChronoUnit.MILLIS));
        if (duration.isNegative()) {
            return Duration.ZERO;
        }
        return duration;
    }

    public boolean hasAccessed(final UUID id) {
        return data.playerAccess.containsKey(id);
    }

    public boolean canAccess(final UUID id) {
        final OffsetDateTime time = data.playerAccess.get(id);
        final OffsetDateTime now = OffsetDateTime.now();
        return time == null || data.refreshInterval != 0 && time.plus(data.refreshInterval, ChronoUnit.MILLIS).isBefore(now);
    }

    public boolean access(final UUID id) {
        final OffsetDateTime time = data.playerAccess.get(id);
        final OffsetDateTime now = OffsetDateTime.now();
        if (time == null || data.refreshInterval != 0 && time.plus(data.refreshInterval, ChronoUnit.MILLIS).isBefore(now)) {
            data.playerAccess.put(id, now);
            setDirty();
            return true;
        }
        return false;
    }

    public long getRefreshInterval(final TimeUnit unit) {
        return unit.convert(data.refreshInterval, TimeUnit.MILLISECONDS);
    }

    public void setRefreshInterval(final long interval, final TimeUnit unit) {
        final long value = unit.toMillis(interval);
        if (value < 0) {
            throw new IllegalArgumentException("Interval can't be lower than 0 in milliseconds!");
        }
        data.refreshInterval = value;
        setDirty();
    }

}
