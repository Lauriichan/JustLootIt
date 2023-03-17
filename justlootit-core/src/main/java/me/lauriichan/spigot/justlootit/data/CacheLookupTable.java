package me.lauriichan.spigot.justlootit.data;

import java.time.OffsetDateTime;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.storage.IModifiable;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.storage.Storage;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;

public class CacheLookupTable extends Storable implements IModifiable {
    
    // TODO: Add multiworld support

    public static final long ID = 0;
    public static final int SIZE = 10;

    public static final long MIN_ENTRY_ID = ID + 1;
    public static final long MAX_ENTRY_ID = MIN_ENTRY_ID + SIZE;

    // TODO: Allow days to be customizable
    public static final int DEFAULT_DAYS = 7;

    public static final StorageAdapter<CacheLookupTable> ADAPTER = new StorageAdapter<>(CacheLookupTable.class, 1) {
        @Override
        public void serialize(CacheLookupTable storable, ByteBuf buffer) {
            int size = storable.tableToMapped.size();
            buffer.writeInt(storable.cacheDays);
            buffer.writeByte(size);
            for (int index = 0; index < size; index++) {
                LookupEntry entry = storable.tableToMapped.get(index);
                buffer.writeLong(entry.mappedId);
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
                entry.mappedId = buffer.readLong();
                entry.cached = DataIO.OFFSET_DATE_TIME.deserialize(buffer);
                table.tableToMapped.put(entry.tableId, entry);
                table.mappedToTable.put(entry.mappedId, entry);
            }
            table.update();
            return table;
        }
    };

    private static final class LookupEntry {

        private int tableId;
        private long mappedId;

        private OffsetDateTime cached;

    }

    private final Int2ObjectOpenHashMap<LookupEntry> tableToMapped = new Int2ObjectOpenHashMap<>(SIZE);
    private final Long2ObjectOpenHashMap<LookupEntry> mappedToTable = new Long2ObjectOpenHashMap<>(SIZE);

    private int cacheDays;

    private boolean dirty = false;

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
            if (entry.cached.plusDays(cacheDays).isBefore(now)) {
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

    public boolean hasMapped(long mappedId) {
        return mappedToTable.containsKey(mappedId);
    }

    public int getTableId(long mappedId) {
        if (!mappedToTable.containsKey(mappedId)) {
            return -1;
        }
        return mappedToTable.get(mappedId).tableId;
    }

    public long getMappedId(int tableId) {
        if (!tableToMapped.containsKey(tableId)) {
            return -1;
        }
        return tableToMapped.get(tableId).mappedId;
    }

    public long getEntryIdByTable(int tableId) {
        if (!tableToMapped.containsKey(tableId)) {
            return -1;
        }
        return tableToMapped.get(tableId).tableId + MIN_ENTRY_ID;
    }

    public long getEntryIdByMapped(long mappedId) {
        if (!mappedToTable.containsKey(mappedId)) {
            return -1;
        }
        return mappedToTable.get(mappedId).tableId + MIN_ENTRY_ID;
    }

    public boolean access(long mappedId) {
        update();
        LookupEntry entry = mappedToTable.get(mappedId);
        if (entry == null) {
            return false;
        }
        if (entry.tableId != tableToMapped.size() - 1) {
            // Push to top
            mappedToTable.remove(entry.tableId);
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

    public long acquire(long mappedId) {
        update();
        LookupEntry entry = mappedToTable.get(mappedId);
        if (entry != null) {
            return entry.tableId;
        }
        return newEntry(mappedId) + MIN_ENTRY_ID;
    }

    private int newEntry(long mappedId) {
        LookupEntry entry = new LookupEntry();
        entry.cached = OffsetDateTime.now().plusDays(7);
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

    public static CacheLookupTable retrieve(Storage<Storable> storage) {
        Storable entry = storage.read(ID);
        if (entry != null) {
            if (entry instanceof CacheLookupTable table) {
                return table;
            }
            throw new IllegalStateException("Storage has unknown object at id " + ID);
        }
        CacheLookupTable table = new CacheLookupTable(DEFAULT_DAYS);
        storage.write(table);
        return table;
    }

}
