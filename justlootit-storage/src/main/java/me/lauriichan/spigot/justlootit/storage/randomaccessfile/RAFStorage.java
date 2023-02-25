package me.lauriichan.spigot.justlootit.storage.randomaccessfile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

import io.netty.buffer.ByteBuf;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.storage.Storage;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;
import me.lauriichan.spigot.justlootit.storage.StorageException;
import me.lauriichan.spigot.justlootit.storage.cache.Int2ObjectCache;
import me.lauriichan.spigot.justlootit.storage.cache.ThreadSafeCache;

public class RAFStorage<S extends Storable> extends Storage<S> {

    private static final long LOOKUP_HEADER_SIZE = Long.BYTES;

    private static final long LOOKUP_ENTRY_ID_SIZE = Short.BYTES;
    private static final long LOOKUP_ENTRY_OFFSET_SIZE = Long.BYTES;
    private static final long LOOKUP_ENTRY_SIZE = LOOKUP_ENTRY_ID_SIZE + LOOKUP_ENTRY_OFFSET_SIZE;

    private static final long VALUE_HEADER_SIZE = Short.BYTES + Integer.BYTES;

    private final File directory;
    private final ThreadSafeCache<Integer, RAFAccess<S>> accesses;

    public RAFStorage(Logger logger, Class<S> baseType, File directory) {
        super(baseType);
        this.accesses = new ThreadSafeCache<>(new Int2ObjectCache<>(logger));
        this.directory = directory;
    }

    @Override
    public void updateEach(Consumer<S> updater) {
        if (!directory.exists()) {
            return;
        }
        File[] files = directory.listFiles(RAFAccess.FILE_FILTER);
        for (File file : files) {
            int fileId;
            try {
                fileId = Integer.parseInt(file.getName().substring(0, file.getName().length() - 4));
            } catch (NumberFormatException nfe) {
                continue;
            }
            if (!accesses.has(fileId)) {
                // TODO: Load and update
            }
        }
    }

    /*
     * Data writing & file creation
     */
    
    @Override
    public void write(S storable) throws StorageException {
        long id = storable.id();
        long possibleId = id >> 10;
        if (Long.compareUnsigned((possibleId | 0xFFFFFFFF), 0xFFFFFFFF) >= 1) {
            throw new StorageException("Unsupported file id '" + Long.toHexString(possibleId) + "'!");
        }
        int fileId = (int) (possibleId & 0xFFFFFFFF);
        short valueId = (short) (id & 0x3FF);
        if (accesses.has(fileId)) {
            write(accesses.get(fileId), valueId, storable);
            return;
        }
        RAFAccess<S> access = new RAFAccess<>(fileId, directory);
        accesses.set(fileId, access);
        write(access, valueId, storable);
    }

    private void write(RAFAccess<S> access, short valueId, S storable) throws StorageException {
        StorageAdapter<? extends S> adapter = findAdapterFor(storable.getClass().asSubclass(baseType));
        if (adapter == null) {
            throw new StorageException("Couldn't find storage adapter for type '" + storable.getClass().getName() + "'!");
        }
        ByteBuf buffer = adapter.serializeValue(adapter.type().cast(storable));
        access.writeLock();
        try {
            RandomAccessFile file = access.open();
            long fileSize = file.length();
            long offsetPosition = 0;
            long headerPosition = 0;
            long lookupPosition = 0;
            if (fileSize != 0) {
                offsetPosition = file.length() - LOOKUP_HEADER_SIZE;
                file.seek(offsetPosition);
                headerPosition = file.readLong();
                file.seek(headerPosition);
                lookupPosition = offsetPosition;
                while (file.getFilePointer() != offsetPosition) {
                    if (file.readShort() != valueId) {
                        file.seek(file.getFilePointer() + LOOKUP_ENTRY_OFFSET_SIZE);
                        continue;
                    }
                    lookupPosition = file.readLong();
                }
            }
            int bufferSize = buffer.readableBytes();
            if (fileSize == 0) {
                file.setLength(bufferSize + LOOKUP_HEADER_SIZE + LOOKUP_ENTRY_SIZE + VALUE_HEADER_SIZE);
                file.seek(file.length() - LOOKUP_HEADER_SIZE);
                long headerOffset = file.getFilePointer() - LOOKUP_HEADER_SIZE;
                file.writeLong(headerOffset);
                file.seek(headerOffset);
                file.writeShort(valueId);
                file.writeLong(0);
                file.seek(0);
                file.writeShort(adapter.typeId());
                file.writeInt(bufferSize);
                file.write(buffer.array());
                return;
            }
            if (lookupPosition != offsetPosition) {
                file.seek(lookupPosition);
                long dataOffset = file.readLong();
                file.seek(dataOffset);
                int dataSize = file.readInt();
                long offset = updateFileSize(file, dataOffset, dataSize + VALUE_HEADER_SIZE, bufferSize + VALUE_HEADER_SIZE);
                file.seek(offsetPosition + offset);
                file.writeLong(headerPosition + offset);
                file.seek(dataOffset);
                file.writeShort(adapter.typeId());
                file.writeInt(bufferSize);
                file.write(buffer.array());
                return;
            }
            long offset = updateFileSize(file, headerPosition, 0, bufferSize + VALUE_HEADER_SIZE + LOOKUP_ENTRY_SIZE);
            file.seek(offsetPosition + offset);
            file.writeLong(headerPosition + offset);
            file.seek(headerPosition + offset - LOOKUP_ENTRY_SIZE);
            file.writeShort(valueId);
            file.writeLong(headerPosition);
            file.seek(headerPosition);
            file.writeShort(adapter.typeId());
            file.writeInt(bufferSize);
            file.write(buffer.array());
        } catch (IOException e) {
            throw new StorageException("Failed to write value with id '" + Long.toHexString(storable.id()) + "' to file!", e);
        } finally {
            access.writeUnlock();
        }
    }
    
    /*
     * Data deletion & file deletion
     */

    @SuppressWarnings("resource")
    @Override
    public boolean delete(long id) throws StorageException {
        long possibleId = id >> 10;
        if (Long.compareUnsigned((possibleId | 0xFFFFFFFF), 0xFFFFFFFF) >= 1) {
            throw new StorageException("Unsupported file id '" + Long.toHexString(possibleId) + "'!");
        }
        int fileId = (int) (possibleId & 0xFFFFFFFF);
        short valueId = (short) (id & 0x3FF);
        if(accesses.has(fileId)) {
            return delete(accesses.get(fileId), valueId);
        }
        RAFAccess<S> access = new RAFAccess<>(fileId, directory);
        if(!access.exists()) {
            return false;
        }
        accesses.set(fileId, access);
        return delete(access, valueId);
    }
    
    private boolean delete(RAFAccess<S> access, short valueId) {
        if(!access.exists()) {
            return false;
        }
        
        return true;
    }
    
    /*
     * Clear data
     */
    
    @Override
    public void clear() throws StorageException {
        List<Integer> list = accesses.keys();
        for(Integer id : list) {
            RAFAccess<S> access = accesses.get(id);
            accesses.remove(id);
        }
    }
    
    /*
     * File size management
     */

    private long updateFileSize(RandomAccessFile file, long offset, long oldSize, long newSize) throws IOException {
        long difference = oldSize - newSize;
        if (difference == 0) {
            return 0;
        }
        if (difference < 0) {
            difference *= -1;
            shrinkFile(file, offset + newSize, difference);
            return difference;
        }
        expandFile(file, offset + oldSize, difference);
        return difference;
    }

    private void expandFile(RandomAccessFile file, long offset, long amount) throws IOException {
        long oldFileEnd = file.length();
        file.setLength(oldFileEnd + amount);
        long pointer = oldFileEnd;
        byte[] buffer = new byte[16384];
        while (pointer != offset) {
            long diff = pointer - offset;
            int size = diff > buffer.length ? buffer.length : (int) diff;
            pointer -= size;
            file.seek(pointer);
            file.read(buffer, 0, size);
            file.seek(pointer + amount);
            file.write(buffer, 0, size);
        }
    }

    private void shrinkFile(RandomAccessFile file, long offset, long amount) throws IOException {
        long pointer = offset + amount;
        long newLength = file.length() - amount;
        byte[] buffer = new byte[16384];
        while (pointer != newLength) {
            long diff = newLength - pointer;
            int size = diff > buffer.length ? buffer.length : (int) diff;
            file.seek(pointer);
            file.read(buffer, 0, size);
            file.seek(pointer - amount);
            file.write(buffer, 0, size);
            pointer += size;
        }
        file.setLength(newLength);
    }
    
    /*
     * Data reading
     */

    @SuppressWarnings("resource")
    @Override
    public S read(long id) throws StorageException {
        long possibleId = id >> 10;
        if (Long.compareUnsigned((possibleId | 0xFFFFFFFF), 0xFFFFFFFF) >= 1) {
            throw new StorageException("Unsupported file id '" + Long.toHexString(possibleId) + "'!");
        }
        int fileId = (int) (possibleId & 0xFFFFFFFF);
        short valueId = (short) (id & 0x3FF);
        if (accesses.has(fileId)) {
            return read(accesses.get(fileId), valueId);
        }
        RAFAccess<S> access = new RAFAccess<>(fileId, directory);
        if(!access.exists()) {
            return null;
        }
        accesses.set(fileId, access);
        return read(access, valueId);
    }

    private S read(RAFAccess<S> access, short valueId) {
        if (!access.exists()) {
            return null;
        }
        return null;
    }
    
    

}
