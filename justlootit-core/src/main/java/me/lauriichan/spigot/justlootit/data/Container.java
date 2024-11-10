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
import me.lauriichan.minecraft.pluginbase.config.ConfigManager;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.config.MainConfig;
import me.lauriichan.spigot.justlootit.config.RefreshConfig;
import me.lauriichan.spigot.justlootit.config.data.RefreshGroup;
import me.lauriichan.spigot.justlootit.config.data.RefreshGroup.UniqueType;
import me.lauriichan.spigot.justlootit.data.io.BufIO;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.storage.IModifiable;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;
import me.lauriichan.spigot.justlootit.storage.StorageAdapterRegistry;

public abstract class Container implements IModifiable {

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
        public final void serialize(final StorageAdapterRegistry registry, final C storable, final ByteBuf buffer) {
            final ContainerData data = storable.data;
            buffer.writeInt(data.playerAccess.size());
            for (final Entry<UUID, Access> entry : data.playerAccess.entrySet()) {
                DataIO.UUID.serialize(buffer, entry.getKey());
                DataIO.OFFSET_DATE_TIME.serialize(buffer, entry.getValue().time());
                buffer.writeInt(entry.getValue().accessCount());
            }
            BufIO.writeString(buffer, data.refreshGroupId);
            serializeSpecial(registry, storable, buffer);
        }

        @Override
        public final C deserialize(final StorageAdapterRegistry registry, final ByteBuf buffer) {
            final ContainerData data = new ContainerData(config);
            final int amount = buffer.readInt();
            for (int index = 0; index < amount; index++) {
                final UUID uuid = DataIO.UUID.deserialize(buffer).value();
                final OffsetDateTime time = DataIO.OFFSET_DATE_TIME.deserialize(buffer).value();
                final int accessCount = buffer.readInt();
                data.playerAccess.put(uuid, new Access(uuid, time, accessCount));
            }
            data.refreshGroupId = BufIO.readString(buffer);
            return deserializeSpecial(registry, data, buffer);
        }

        protected abstract void serializeSpecial(final StorageAdapterRegistry registry, final C storable, final ByteBuf buffer);

        protected abstract C deserializeSpecial(final StorageAdapterRegistry registry, final ContainerData data, final ByteBuf buffer);

    }
    
    public static final class Access {
        
        private final UUID uuid;
        private volatile OffsetDateTime time;
        private volatile int accessCount;
        
        private Access(UUID uuid, OffsetDateTime time) {
            this(uuid, time, 1);
        }
        
        private Access(UUID uuid, OffsetDateTime time, int accessCount) {
            this.uuid = uuid;
            this.time = time;
            this.accessCount = accessCount;
        }
        
        public UUID uuid() {
            return uuid;
        }
        
        public OffsetDateTime time() {
            return time;
        }
        
        public int accessCount() {
            return accessCount;
        }
        
    }

    protected static final class ContainerData {

        private final RefreshConfig config;
        private final Object2ObjectMap<UUID, Access> playerAccess = Object2ObjectMaps
            .synchronize(new Object2ObjectLinkedOpenHashMap<>());

        private WeakReference<RefreshGroup> refreshGroup;
        private volatile String refreshGroupId;

        public ContainerData(RefreshConfig config) {
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
                JustLootItPlugin.get().logger().warning("Refresh group '{0}' doesn't exist", refreshGroupId);
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
    
    final MainConfig mainConfig;

    final ContainerData data;
    private boolean dirty = false;

    public Container() {
        ConfigManager manager = JustLootItPlugin.get().configManager();
        this.mainConfig = manager.config(MainConfig.class);
        this.data = new ContainerData(manager.config(RefreshConfig.class));
    }

    public Container(final ContainerData data) {
        this.mainConfig = JustLootItPlugin.get().configManager().config(MainConfig.class);
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

    public ObjectSet<Entry<UUID, Access>> accesses() {
        return data.playerAccess.entrySet();
    }

    public void resetAccess(final UUID id) {
        if (data.playerAccess.remove(id) != null) {
            setDirty();
        }
    }
    
    public void decreaseAccessCount(final UUID id) {
        Access access = data.playerAccess.get(id);;
        if (access == null) {
            return;
        }
        access.time = OffsetDateTime.MIN;
        if ((access.accessCount -= 1) <= 0) {
            data.playerAccess.remove(id);
        }
        setDirty();
    }

    public boolean hasAccessed(final UUID id) {
        return data.playerAccess.containsKey(id);
    }
    
    public Access getAccess(final UUID id) {
        return data.playerAccess.get(id);
    }

    public OffsetDateTime getAccessTime(final UUID id) {
        Access access = data.playerAccess.get(id);
        if (access == null) {
            return null;
        }
        return access.time();
    }

    public int getAccessCount(final UUID id) {
        Access access = data.playerAccess.get(id);
        if (access == null) {
            return 0;
        }
        return access.accessCount();
    }

    public Duration durationUntilNextAccess(final UUID id) {
        RefreshGroup group = data.group();
        if (group == null) {
            return Duration.ofSeconds(-1);
        }
        return group.duration(getAccessTime(id), OffsetDateTime.now());
    }

    public boolean canAccess(final UUID id) {
        RefreshGroup group = data.group();
        if (group == null) {
            return !data.playerAccess.containsKey(id);
        }
        return group.isAccessible(getAccessTime(id), OffsetDateTime.now());
    }

    public boolean access(final UUID id) {
        RefreshGroup group = data.group();
        if (group == null) {
            if (!data.playerAccess.containsKey(id)) {
                data.playerAccess.put(id, new Access(id, OffsetDateTime.now()));
                setDirty();
                return true;
            }
            return false;
        }
        final OffsetDateTime now = OffsetDateTime.now();
        Access access = getAccess(id);
        if (access == null) {
            data.playerAccess.put(id, new Access(id, now));
            setDirty();
            return true;
        }
        if (group.isAccessible(access.time(), now)) {
            access.time = now;
            access.accessCount += 1;
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

    public long generateSeed(PlayerAdapter player, long seed) {
        RefreshGroup group = data.group();
        boolean unique = mainConfig.uniqueLootPerPlayer();
        boolean incremental = false;
        if (group != null) {
            if (group.unique() != UniqueType.GLOBAL) {
                unique = group.unique() == UniqueType.TRUE;
            }
            incremental = group.incremental();
        }
        if (unique) {
            seed *= player.getUniqueSeed();
        }
        if (incremental) {
            seed *= (getAccessCount(player.getUniqueId()) + 1);
        }
        return seed;
    }
    
}
