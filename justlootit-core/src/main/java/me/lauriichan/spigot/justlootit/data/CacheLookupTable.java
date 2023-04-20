package me.lauriichan.spigot.justlootit.data;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.bukkit.World;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
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
    public static final long MAX_ENTRY_ID = MIN_ENTRY_ID + SIZE;
    
    public static final int DEFAULT_DAYS = 7;

    public static final StorageAdapter<CacheLookupTable> ADAPTER = new StorageAdapter<>(CacheLookupTable.class, 1) {
        @Override
        public void serialize(CacheLookupTable storable, ByteBuf buffer) {
            int size = storable.tableToMapped.size();
            buffer.writeInt(storable.cacheDays);
            buffer.writeByte(size);
            for (int index = 0; index < size; index++) {
                LookupEntry entry = storable.tableToMapped.get(index);
                DataIO.UUID.serialize(buffer, entry.mappedId.worldId());
                buffer.writeLong(entry.mappedId.containerId());
                DataIO.OFFSET_DATE_TIME.serialize(buffer, entry.cached);
            }
        }

        @Override
        public CacheLookupTable deserialize(long id, ByteBuf buffer) {
            CacheLookupTable table = new CacheLookupTable(buffer.readInt());
            int size = buffer.readByte();
            for (int index = 0; index < size; index++) {
                LookupEntry entry = new LookupEntry();
                entry.tableId = index;
                UUID worldId = DataIO.UUID.deserialize(buffer);
                long containerId = buffer.readLong();
                entry.mappedId = new WorldEntry(worldId, containerId);
                entry.cached = DataIO.OFFSET_DATE_TIME.deserialize(buffer);
                table.tableToMapped.put(entry.tableId, entry);
                table.mappedToTable.put(entry.mappedId, entry);
            }
            table.update();
            return table;
        }
    };
    
    public static final record WorldEntry(UUID worldId, long containerId) {
        public static final WorldEntry INVALID = new WorldEntry((UUID) null, -1);
        public WorldEntry(World world, long containerId) {
            this(world.getUID(), containerId);
        }
        public boolean isValid() {
            return worldId != null && containerId >= 0;
        }
    }

    private static final class LookupEntry {

        private int tableId;
        private WorldEntry mappedId;

        private OffsetDateTime cached;

    }

    private final Int2ObjectArrayMap<LookupEntry> tableToMapped = new Int2ObjectArrayMap<>(SIZE);
    private final Object2ObjectArrayMap<WorldEntry, LookupEntry> mappedToTable = new Object2ObjectArrayMap<>(SIZE);

    private int cacheDays;

    private boolean dirty = false;

    public CacheLookupTable() {
        this(DEFAULT_DAYS);
    }

    public CacheLookupTable(int cacheDays) {
        super(ID);
        this.cacheDays = Math.max(cacheDays, 0);
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    public void update() {
        int offset = 0;
        OffsetDateTime now = OffsetDateTime.now();
        for (int index = 0; index < SIZE; index++) {
            LookupEntry entry = tableToMapped.remove(index);
            if (entry == null) {
                // Null means we're done
                break;
            }
            if (now.isAfter(entry.cached.plusDays(cacheDays))) {
                mappedToTable.remove(entry.mappedId);
                offset++;
                continue;
            }
            tableToMapped.put(index - offset, entry);
        }
        if(offset != 0) {
            setDirty();
        }
    }

    private void setDirty() {
        dirty = true;
    }

    public int getCacheDays() {
        return cacheDays;
    }

    public void setCacheDays(int cacheDays) {
        this.cacheDays = Math.max(cacheDays, 0);
        setDirty();
    }

    public boolean hasTable(int tableId) {
        return tableToMapped.containsKey(tableId);
    }

    public boolean hasMapped(WorldEntry mappedId) {
        return mappedToTable.containsKey(mappedId);
    }

    public int getTableId(WorldEntry mappedId) {
        if (!mappedToTable.containsKey(mappedId)) {
            return -1;
        }
        return mappedToTable.get(mappedId).tableId;
    }

    public WorldEntry getMappedId(int tableId) {
        if (!tableToMapped.containsKey(tableId)) {
            return WorldEntry.INVALID;
        }
        return tableToMapped.get(tableId).mappedId;
    }

    public long getEntryIdByTable(int tableId) {
        if (!tableToMapped.containsKey(tableId)) {
            return -1;
        }
        return tableToMapped.get(tableId).tableId + MIN_ENTRY_ID;
    }

    public long getEntryIdByMapped(WorldEntry mappedId) {
        if (!mappedToTable.containsKey(mappedId)) {
            return -1;
        }
        return mappedToTable.get(mappedId).tableId + MIN_ENTRY_ID;
    }

    public boolean access(WorldEntry mappedId) {
        update();
        LookupEntry entry = mappedToTable.get(mappedId);
        if (entry == null) {
            return false;
        }
        if (entry.tableId != tableToMapped.size() - 1) {
            // Push to top
            tableToMapped.remove(entry.tableId);
            for (int index = entry.tableId + 1; index < tableToMapped.size(); index++) {
                LookupEntry current = tableToMapped.remove(index);
                tableToMapped.put(--current.tableId, current);
            }
            entry.tableId = tableToMapped.size() - 1;
            tableToMapped.put(entry.tableId, entry);
            setDirty();
        }
        return true;
    }

    public long acquire(WorldEntry mappedId) {
        update();
        LookupEntry entry = mappedToTable.get(mappedId);
        if (entry != null) {
            return entry.tableId;
        }
        return newEntry(mappedId) + MIN_ENTRY_ID;
    }

    private int newEntry(WorldEntry mappedId) {
        LookupEntry entry = new LookupEntry();
        entry.cached = OffsetDateTime.now();
        entry.mappedId = mappedId;
        if (tableToMapped.size() == SIZE) {
            entry.tableId = SIZE - 1;
            LookupEntry first = tableToMapped.remove(0);
            mappedToTable.remove(first.mappedId);
            for (int index = 1; index < SIZE; index++) {
                LookupEntry current = tableToMapped.remove(index);
                tableToMapped.put(--current.tableId, current);
            }
        } else {
            entry.tableId = tableToMapped.size();
        }
        tableToMapped.put(entry.tableId, entry);
        mappedToTable.put(mappedId, entry);
        setDirty();
        return entry.tableId;
    }

    public static CacheLookupTable retrieve(IStorage<Storable> storage) {
        Storable entry = storage.read(ID);
        if (entry != null) {
            if (entry instanceof CacheLookupTable table) {
                return table;
            }
            throw new IllegalStateException("Storage has unknown object at id " + ID);
        }
        // TODO: Customize how many days this table should have
        CacheLookupTable table = new CacheLookupTable();
        storage.write(table);
        return table;
    }

}
