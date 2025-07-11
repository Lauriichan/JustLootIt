package me.lauriichan.spigot.justlootit.data;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.bukkit.World;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.config.MainConfig;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.storage.IModifiable;
import me.lauriichan.spigot.justlootit.storage.IStorage;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;
import me.lauriichan.spigot.justlootit.storage.StorageAdapterRegistry;
import me.lauriichan.spigot.justlootit.storage.Stored;

public class CacheLookupTable implements IModifiable {

    public static final long ID = 15;
    public static final long MIN_ENTRY_ID = ID + 1;

    public static final int DEFAULT_DAYS = 7;

    public static final StorageAdapter<CacheLookupTable> ADAPTER = new StorageAdapter<>(CacheLookupTable.class, 1) {
        @Override
        public void serialize(final StorageAdapterRegistry registry, final CacheLookupTable storable, final ByteBuf buffer) {
            final int size = storable.tableToMapped.size();
            buffer.writeByte(size);
            for (int index = 0; index < size; index++) {
                final LookupEntry entry = storable.tableToMapped.get(index);
                buffer.writeLong(entry.entryId);
                DataIO.UUID.serialize(buffer, entry.mappedId.worldId());
                buffer.writeLong(entry.mappedId.containerId());
                DataIO.OFFSET_DATE_TIME.serialize(buffer, entry.cached);
            }
        }

        @Override
        public CacheLookupTable deserialize(final StorageAdapterRegistry registry, final ByteBuf buffer) {
            final int size = buffer.readByte();
            final CacheLookupTable table = new CacheLookupTable(size);
            for (int index = 0; index < size; index++) {
                final LookupEntry entry = new LookupEntry();
                entry.tableId = index;
                entry.entryId = buffer.readLong();
                final UUID worldId = DataIO.UUID.deserialize(buffer).value();
                final long containerId = buffer.readLong();
                entry.mappedId = new WorldEntry(worldId, containerId);
                entry.cached = DataIO.OFFSET_DATE_TIME.deserialize(buffer).value();
                if(table.entryIds.contains(entry.entryId)) {
                    continue;
                }
                table.entryIds.add(entry.entryId);
                table.tableToMapped.put(entry.tableId, entry);
                table.mappedToTable.put(entry.mappedId, entry);
            }
            table.update();
            return table;
        }
    };

    public static final record WorldEntry(UUID worldId, long containerId) {

        public static final WorldEntry INVALID = new WorldEntry((UUID) null, -1);
        public WorldEntry(final World world, final long containerId) {
            this(world.getUID(), containerId);
        }

        public boolean isValid() {
            return worldId != null && containerId >= 0;
        }
    }

    private static final class LookupEntry {

        private int tableId;
        private long entryId;
        private WorldEntry mappedId;

        private OffsetDateTime cached;

    }

    private final Int2ObjectArrayMap<LookupEntry> tableToMapped;
    private final Object2ObjectArrayMap<WorldEntry, LookupEntry> mappedToTable;
    
    private final LongArrayList entryIds;

    private final MainConfig config = JustLootItPlugin.get().configManager().config(MainConfig.class);

    private boolean dirty = false;
    
    private int maxSize;
    private long maxEntryId;

    public CacheLookupTable(int size) {
        this.maxSize = Math.max(size, config.playerInventoryCacheSize());
        this.maxEntryId = MIN_ENTRY_ID + maxSize;
        this.tableToMapped = new Int2ObjectArrayMap<>(maxSize);
        this.mappedToTable = new Object2ObjectArrayMap<>(maxSize);
        this.entryIds = new LongArrayList(maxSize);
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    public void update() {
        int days = config.playerInventoryDayTimeout();
        this.maxSize = config.playerInventoryCacheSize();
        this.maxEntryId = MIN_ENTRY_ID + maxSize;
        if (days == 0) {
            return;
        }
        int tableSize = tableToMapped.size();
        if (tableSize > maxSize) {
            for (int i = maxSize; i < tableSize; i++) {
                LookupEntry entry = tableToMapped.remove(i);
                if (entry == null) {
                    continue;
                }
                mappedToTable.remove(entry.mappedId);
                entryIds.rem(entry.entryId);
            }
        }
        int offset = 0;
        final OffsetDateTime now = OffsetDateTime.now();
        for (int index = 0; index < maxSize; index++) {
            final LookupEntry entry = tableToMapped.remove(index);
            if (entry == null) {
                // Null means we're done
                break;
            }
            if (now.isAfter(entry.cached.plusDays(days))) {
                mappedToTable.remove(entry.mappedId);
                entryIds.rem(entry.entryId);
                offset++;
                continue;
            }
            tableToMapped.put(index - offset, entry);
        }
        if (offset != 0) {
            setDirty();
        }
    }

    private void setDirty() {
        dirty = true;
    }

    public boolean hasTable(final int tableId) {
        return tableToMapped.containsKey(tableId);
    }
    
    public boolean hasEntry(final long entryId) {
        return entryIds.contains(entryId);
    }

    public boolean hasMapped(final WorldEntry mappedId) {
        return mappedToTable.containsKey(mappedId);
    }

    public int getTableId(final WorldEntry mappedId) {
        if (!mappedToTable.containsKey(mappedId)) {
            return -1;
        }
        return mappedToTable.get(mappedId).tableId;
    }

    public WorldEntry getMappedId(final int tableId) {
        if (!tableToMapped.containsKey(tableId)) {
            return WorldEntry.INVALID;
        }
        return tableToMapped.get(tableId).mappedId;
    }

    public long getEntryIdByTable(final int tableId) {
        if (!tableToMapped.containsKey(tableId)) {
            return -1;
        }
        return tableToMapped.get(tableId).entryId;
    }

    public long getEntryIdByMapped(final WorldEntry mappedId) {
        if (!mappedToTable.containsKey(mappedId)) {
            return -1;
        }
        return mappedToTable.get(mappedId).entryId;
    }
    
    public void drop(final WorldEntry mappedId) {
        LookupEntry entry = mappedToTable.remove(mappedId);
        if (entry == null) {
            return;
        }
        entryIds.rem(entry.entryId);
    }

    public boolean access(final WorldEntry mappedId) {
        update();
        final LookupEntry entry = mappedToTable.get(mappedId);
        if (entry == null) {
            return false;
        }
        if (entry.tableId != tableToMapped.size() - 1) {
            // Push to top
            tableToMapped.remove(entry.tableId);
            for (int index = entry.tableId + 1; index < tableToMapped.size(); index++) {
                final LookupEntry current = tableToMapped.remove(index);
                tableToMapped.put(--current.tableId, current);
            }
            entry.tableId = tableToMapped.size() - 1;
            tableToMapped.put(entry.tableId, entry);
            setDirty();
        }
        return true;
    }

    public long acquire(final WorldEntry mappedId) {
        update();
        final LookupEntry entry = mappedToTable.get(mappedId);
        if (entry != null) {
            return entry.entryId;
        }
        return newEntry(mappedId);
    }

    private long newEntry(final WorldEntry mappedId) {
        final LookupEntry entry = new LookupEntry();
        entry.cached = OffsetDateTime.now();
        entry.mappedId = mappedId;
        if (tableToMapped.size() == maxSize) {
            entry.tableId = maxSize - 1;
            final LookupEntry first = tableToMapped.remove(0);
            mappedToTable.remove(first.mappedId);
            for (int index = 1; index < maxSize; index++) {
                final LookupEntry current = tableToMapped.remove(index);
                tableToMapped.put(--current.tableId, current);
            }
            entry.entryId = first.entryId;
        } else {
            entry.tableId = tableToMapped.size();
            entry.entryId = findEntryId();
            entryIds.add(entry.entryId);
        }
        tableToMapped.put(entry.tableId, entry);
        mappedToTable.put(mappedId, entry);
        setDirty();
        return entry.entryId;
    }

    private long findEntryId() {
        for (long id = MIN_ENTRY_ID; id < maxEntryId; id++) {
            if (entryIds.contains(id)) {
                continue;
            }
            return id;
        }
        return maxEntryId;
    }

    public static CacheLookupTable retrieve(final JustLootItPlugin plugin, final IStorage storage) {
        final Stored<CacheLookupTable> entry = storage.read(ID);
        if (entry != null) {
            return entry.value();
        }
        final CacheLookupTable table = new CacheLookupTable(0);
        storage.write(storage.registry().create(table).id(ID));
        return table;
    }

}
