package me.lauriichan.spigot.justlootit.data;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.bukkit.World;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.storage.IModifiable;
import me.lauriichan.spigot.justlootit.storage.IStorage;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;

public class CacheLookupTable extends Storable implements IModifiable {

    public static final long ID = 0;
    public static final int SIZE = 20;

    public static final long MIN_ENTRY_ID = ID + 1;
    public static final long MAX_ENTRY_ID = MIN_ENTRY_ID + SIZE - 1;

    public static final int DEFAULT_DAYS = 7;

    public static final StorageAdapter<CacheLookupTable> ADAPTER = new StorageAdapter<>(CacheLookupTable.class, 1) {
        @Override
        public void serialize(final CacheLookupTable storable, final ByteBuf buffer) {
            final int size = storable.tableToMapped.size();
            buffer.writeInt(storable.cacheDays);
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
        public CacheLookupTable deserialize(final long id, final ByteBuf buffer) {
            final CacheLookupTable table = new CacheLookupTable(buffer.readInt());
            final int size = buffer.readByte();
            for (int index = 0; index < size; index++) {
                final LookupEntry entry = new LookupEntry();
                entry.tableId = index;
                entry.entryId = buffer.readLong();
                final UUID worldId = DataIO.UUID.deserialize(buffer);
                final long containerId = buffer.readLong();
                entry.mappedId = new WorldEntry(worldId, containerId);
                entry.cached = DataIO.OFFSET_DATE_TIME.deserialize(buffer);
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

    private final Int2ObjectArrayMap<LookupEntry> tableToMapped = new Int2ObjectArrayMap<>(SIZE);
    private final Object2ObjectArrayMap<WorldEntry, LookupEntry> mappedToTable = new Object2ObjectArrayMap<>(SIZE);
    
    private final LongArrayList entryIds = new LongArrayList(SIZE);

    private int cacheDays;

    private boolean dirty = false;

    public CacheLookupTable() {
        this(DEFAULT_DAYS);
    }

    public CacheLookupTable(final int cacheDays) {
        super(ID);
        this.cacheDays = Math.max(cacheDays, 0);
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    public void update() {
        int offset = 0;
        final OffsetDateTime now = OffsetDateTime.now();
        for (int index = 0; index < SIZE; index++) {
            final LookupEntry entry = tableToMapped.remove(index);
            if (entry == null) {
                // Null means we're done
                break;
            }
            if (now.isAfter(entry.cached.plusDays(cacheDays))) {
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

    public int getCacheDays() {
        return cacheDays;
    }

    public void setCacheDays(final int cacheDays) {
        this.cacheDays = Math.max(cacheDays, 0);
        setDirty();
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
        if (tableToMapped.size() == SIZE) {
            entry.tableId = SIZE - 1;
            final LookupEntry first = tableToMapped.remove(0);
            mappedToTable.remove(first.mappedId);
            for (int index = 1; index < SIZE; index++) {
                final LookupEntry current = tableToMapped.remove(index);
                tableToMapped.put(--current.tableId, current);
            }
            entry.entryId = first.entryId;
        } else {
            entry.tableId = tableToMapped.size();
            entry.entryId = findEntryId();
        }
        tableToMapped.put(entry.tableId, entry);
        mappedToTable.put(mappedId, entry);
        setDirty();
        return entry.entryId;
    }

    private long findEntryId() {
        for (long id = MIN_ENTRY_ID; id < MAX_ENTRY_ID; id++) {
            if (entryIds.contains(id)) {
                continue;
            }
            return id;
        }
        return MAX_ENTRY_ID;
    }

    public static CacheLookupTable retrieve(final IStorage<Storable> storage) {
        final Storable entry = storage.read(ID);
        if (entry != null) {
            if (entry instanceof final CacheLookupTable table) {
                return table;
            }
            throw new IllegalStateException("Storage has unknown object at id " + ID);
        }
        // TODO: Customize how many days this table should have
        final CacheLookupTable table = new CacheLookupTable();
        storage.write(table);
        return table;
    }

}
