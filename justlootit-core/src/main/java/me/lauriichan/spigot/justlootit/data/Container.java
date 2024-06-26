package me.lauriichan.spigot.justlootit.data;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.config.RefreshConfig;
import me.lauriichan.spigot.justlootit.config.data.RefreshGroup;
import me.lauriichan.spigot.justlootit.data.io.BufIO;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.storage.IModifiable;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;

public abstract class Container extends Storable implements IModifiable {
    
    private static final Object2ObjectArrayMap<Class<? extends Container>, ContainerType> CONTAINERS;
    
    static {
        CONTAINERS = new Object2ObjectArrayMap<>();
        for (ContainerType type : ContainerType.values()) {
            CONTAINERS.put(type.containerType(), type);
        }
    }
    
    public static ContainerType type(Class<? extends Container> containerType) {
        return CONTAINERS.get(containerType);
    }

    protected static abstract class BaseAdapter<C extends Container> extends StorageAdapter<C> {

        private final RefreshConfig config;

        public BaseAdapter(final Class<C> type, final int typeId) {
            super(type, typeId);
            this.config = JustLootItPlugin.get().configManager().config(RefreshConfig.class);
        }

        @Override
        public final void serialize(final C storable, final ByteBuf buffer) {
            final ContainerData data = storable.data;
            buffer.writeInt(data.playerAccess.size());
            for (final Entry<UUID, OffsetDateTime> entry : data.playerAccess.entrySet()) {
                DataIO.UUID.serialize(buffer, entry.getKey());
                DataIO.OFFSET_DATE_TIME.serialize(buffer, entry.getValue());
            }
            BufIO.writeString(buffer, data.refreshGroupId);
            serializeSpecial(storable, buffer);
        }

        @Override
        public final C deserialize(final long id, final ByteBuf buffer) {
            final ContainerData data = new ContainerData(id, config);
            final int amount = buffer.readInt();
            for (int index = 0; index < amount; index++) {
                final UUID uuid = DataIO.UUID.deserialize(buffer).value();
                final OffsetDateTime time = DataIO.OFFSET_DATE_TIME.deserialize(buffer).value();
                data.playerAccess.put(uuid, time);
            }
            data.refreshGroupId = BufIO.readString(buffer);
            return deserializeSpecial(id, data, buffer);
        }

        protected abstract void serializeSpecial(C storable, ByteBuf buffer);

        protected abstract C deserializeSpecial(long id, ContainerData data, ByteBuf buffer);

    }

    protected static final class ContainerData {

        private final long id;
        private final RefreshConfig config;
        private final Object2ObjectMap<UUID, OffsetDateTime> playerAccess = Object2ObjectMaps.synchronize(new Object2ObjectLinkedOpenHashMap<>());

        private WeakReference<RefreshGroup> refreshGroup;
        private volatile String refreshGroupId;

        public ContainerData(long id, RefreshConfig config) {
            this.id = id;
            this.config = config;
        }

        public RefreshGroup group() {
            if (refreshGroupId == null) {
                return null;
            }
            if (refreshGroup != null) {
                return refreshGroup.get();
            }
            RefreshGroup group = config.group(refreshGroupId);
            refreshGroup = new WeakReference<>(group);
            if (group == null) {
                JustLootItPlugin.get().logger().warning("Refresh group '{0}' doesn't exist but is requested by container with id {1}",
                    refreshGroupId, id);
            }
            return group;
        }

        public boolean group(String id) {
            if (Objects.equals(refreshGroupId, id)) {
                return false;
            }
            refreshGroupId = id;
            refreshGroup = null;
            return true;
        }

    }

    final ContainerData data;
    private boolean dirty = false;

    public Container(final long id) {
        this(id, new ContainerData(id, JustLootItPlugin.get().configManager().config(RefreshConfig.class)));
    }

    public Container(final long id, final ContainerData data) {
        super(id);
        this.data = data;
    }
    
    public final ContainerType type() {
        return type(getClass());
    }

    @Override
    public final boolean isDirty() {
        return dirty;
    }

    protected final void setDirty() {
        this.dirty = true;
    }
    
    public int accessAmount() {
        return data.playerAccess.size();
    }
    
    public ObjectSet<Entry<UUID, OffsetDateTime>> accesses() {
        return data.playerAccess.entrySet();
    }
    
    public void resetAccess(final UUID id) {
        if (data.playerAccess.remove(id) != null) {
            setDirty();
        }
    }

    public boolean hasAccessed(final UUID id) {
        return data.playerAccess.containsKey(id);
    }

    public OffsetDateTime getAccessTime(final UUID id) {
        return data.playerAccess.get(id);
    }

    public Duration durationUntilNextAccess(final UUID id) {
        RefreshGroup group = data.group();
        if (group == null) {
            return Duration.ofSeconds(-1);
        }
        return group.duration(data.playerAccess.get(id), OffsetDateTime.now());
    }

    public boolean canAccess(final UUID id) {
        RefreshGroup group = data.group();
        if (group == null) {
            return !data.playerAccess.containsKey(id);
        }
        return group.isAccessible(data.playerAccess.get(id), OffsetDateTime.now());
    }

    public boolean access(final UUID id) {
        RefreshGroup group = data.group();
        if (group == null) {
            if (!data.playerAccess.containsKey(id)) {
                data.playerAccess.put(id, OffsetDateTime.now());
                setDirty();
                return true;
            }
            return false;
        }
        final OffsetDateTime now = OffsetDateTime.now();
        if (group.isAccessible(data.playerAccess.get(id), now)) {
            data.playerAccess.put(id, now);
            setDirty();
            return true;
        }
        return false;
    }
    
    public RefreshGroup group() {
        return data.group();
    }
    
    public boolean hasGroupId() {
        return data.refreshGroupId != null;
    }

    public String getGroupId() {
        return data.refreshGroupId;
    }
    
    public void setGroupId(String id) {
        if (data.group(id)) {
            setDirty();
        }
    }
    
    public abstract ItemEditor createIcon();

}
