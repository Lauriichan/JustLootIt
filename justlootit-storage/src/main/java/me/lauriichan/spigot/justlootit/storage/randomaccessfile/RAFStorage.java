package me.lauriichan.spigot.justlootit.storage.randomaccessfile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.storage.Storage;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;
import me.lauriichan.spigot.justlootit.storage.StorageException;
import me.lauriichan.spigot.justlootit.storage.cache.Int2ObjectCache;
import me.lauriichan.spigot.justlootit.storage.cache.ThreadSafeCache;

public class RAFStorage<S extends Storable> extends Storage<S> {

    private static final int COPY_BUFFER_SIZE = 1024 * 64;
    
    /* This will be rounded up to the next power of two if this isn't a power of two already */
    private static final int VALUE_ID_AMOUNT = 1025;
    
    private static final int VALUE_ID_BITS = Integer.bitCount(VALUE_ID_AMOUNT - 1);
    private static final int VALUE_ID_MASK = -1 >> (Integer.SIZE - VALUE_ID_BITS);

    private static final int LOOKUP_AMOUNT_SIZE = Short.BYTES;
    private static final int LOOKUP_ENTRY_SIZE = Long.BYTES;
    private static final int LOOKUP_HEADER_SIZE = (VALUE_ID_MASK + 1)  * LOOKUP_ENTRY_SIZE + LOOKUP_AMOUNT_SIZE;

    private static final int VALUE_HEADER_ID_SIZE = Short.BYTES;
    private static final int VALUE_HEADER_LENGTH_SIZE = Integer.BYTES;
    private static final int VALUE_HEADER_SIZE = VALUE_HEADER_ID_SIZE + VALUE_HEADER_LENGTH_SIZE;
    
    private static final long INVALID_HEADER_OFFSET = 0L;

    private final File directory;
    private final ThreadSafeCache<Integer, RAFAccess<S>> accesses;

    private final Logger logger;

    public RAFStorage(Logger logger, Class<S> baseType, File directory) {
        super(baseType);
        this.logger = logger;
        this.accesses = new ThreadSafeCache<>(new Int2ObjectCache<>(logger));
        this.directory = directory;
    }

    /*
     * Clear data
     */

    @Override
    public void clear() {
        List<Integer> list = accesses.keys();
        for (Integer id : list) {
            RAFAccess<S> access = accesses.remove(id);
            if (access == null) {
                continue;
            }
            if (access.isOpen()) {
                access.writeLock();
                try {
                    access.close();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Couldn't close File access to '" + access.hexId() + "'");
                } finally {
                    access.writeUnlock();
                }
            }
            access.file().delete();
        }
    }

    @Override
    public void close() {
        List<Integer> list = accesses.keys();
        for (Integer id : list) {
            RAFAccess<S> access = accesses.remove(id);
            if (access == null || !access.isOpen()) {
                continue;
            }
            access.writeLock();
            try {
                access.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Couldn't close File access to '" + access.hexId() + "'");
            } finally {
                access.writeUnlock();
            }
        }
    }

    /*
     * Data reading
     */

    @SuppressWarnings("resource")
    @Override
    public S read(long id) throws StorageException {
        long possibleId = id >> VALUE_ID_BITS;
        if (Long.compareUnsigned((possibleId | 0xFFFFFFFF), 0xFFFFFFFF) >= 1) {
            throw new StorageException("Unsupported file id '" + Long.toHexString(possibleId) + "'!");
        }
        int fileId = (int) (possibleId & 0xFFFFFFFF);
        short valueId = (short) (id & VALUE_ID_MASK);
        if (accesses.has(fileId)) {
            return read(accesses.get(fileId), id, valueId);
        }
        RAFAccess<S> access = new RAFAccess<>(fileId, directory);
        if (!access.exists()) {
            return null;
        }
        accesses.set(fileId, access);
        return read(access, id, valueId);
    }

    private S read(RAFAccess<S> access, long fullId, short valueId) {
        access.readLock();
        try {
            RandomAccessFile file = access.open();
            long fileSize = file.length();
            if (fileSize == 0) {
                accesses.remove(access.id());
                access.close();
                access.file().delete();
                return null;
            }
            long headerOffset = LOOKUP_AMOUNT_SIZE + LOOKUP_ENTRY_SIZE * valueId;
            file.seek(headerOffset);
            long lookupPosition = file.readLong();
            if(lookupPosition == INVALID_HEADER_OFFSET) {
                access.readUnlock();
                return null;
            }
            file.seek(lookupPosition);
            short typeId = file.readShort();
            int bufferSize = file.readInt();
            StorageAdapter<? extends S> adapter = findAdapterFor(typeId);
            if (adapter == null) {
                access.readUnlock();
                access.writeLock();
                try {
                    if(deleteEntry(file, lookupPosition, bufferSize, headerOffset)) {
                        accesses.remove(access.id());
                        access.close();
                        access.file().delete();
                    }
                } catch(IOException e) {
                    throw new StorageException("Failed to delete value with id '" + Long.toHexString(fullId) + "', because of the type " + typeId +" is unknown, from file!", e);
                } finally {
                    access.writeUnlock();
                }
                throw new StorageException("Failed to read value with id '" + Long.toHexString(fullId) + "' from file because the type "
                    + typeId + " is unknown!");
            }
            byte[] rawBuffer = new byte[bufferSize];
            file.read(rawBuffer);
            access.readUnlock();
            return adapter.deserialize(fullId, Unpooled.wrappedBuffer(rawBuffer));
        } catch (IOException e) {
            access.readUnlock();
            throw new StorageException("Failed to read value with id '" + Long.toHexString(fullId) + "' from file!", e);
        }
    }

    /*
     * Data writing & file creation
     */

    @Override
    public void write(S storable) throws StorageException {
        long id = storable.id();
        long possibleId = id >> VALUE_ID_BITS;
        if (Long.compareUnsigned((possibleId | 0xFFFFFFFF), 0xFFFFFFFF) >= 1) {
            throw new StorageException("Unsupported file id '" + Long.toHexString(possibleId) + "'!");
        }
        int fileId = (int) (possibleId & 0xFFFFFFFF);
        short valueId = (short) (id & VALUE_ID_MASK);
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
            if (fileSize == 0) {
                int bufferSize = buffer.readableBytes();
                file.setLength(LOOKUP_HEADER_SIZE + bufferSize);
                fileSize = file.length();
                file.seek(0);
                file.writeShort(1);
                file.skipBytes(LOOKUP_ENTRY_SIZE * valueId);
                file.writeLong(LOOKUP_HEADER_SIZE);
                file.seek(LOOKUP_HEADER_SIZE);
                file.writeShort(adapter.typeId());
                file.writeInt(bufferSize);
                buffer.readBytes(file.getChannel(), bufferSize);
                return;
            }
            file.seek(0);
            short amount = file.readShort();
            file.seek(0);
            file.writeShort(amount + 1);
            long headerOffset = LOOKUP_AMOUNT_SIZE + LOOKUP_ENTRY_SIZE * valueId;
            file.seek(headerOffset);
            long lookupPosition = file.readLong();
            int bufferSize = buffer.readableBytes();
            if (lookupPosition != INVALID_HEADER_OFFSET) {
                file.seek(lookupPosition + VALUE_HEADER_ID_SIZE);
                int dataSize = file.readInt();
                long offset = updateFileSize(file, lookupPosition, dataSize + VALUE_HEADER_SIZE, bufferSize + VALUE_HEADER_SIZE);
                long newDataEnd = lookupPosition + bufferSize + VALUE_HEADER_SIZE;
                if (offset != 0) {
                    file.seek(LOOKUP_AMOUNT_SIZE);
                    while (file.getFilePointer() != LOOKUP_HEADER_SIZE) {
                        long entryOffset = file.readLong();
                        if (entryOffset < newDataEnd) {
                            continue;
                        }
                        file.seek(file.getFilePointer() - LOOKUP_ENTRY_SIZE);
                        file.writeLong(entryOffset + offset);
                    }
                }
                file.seek(lookupPosition);
                file.writeShort(adapter.typeId());
                file.writeInt(bufferSize);
                buffer.readBytes(file.getChannel(), bufferSize);
                return;
            }
            file.setLength(fileSize + bufferSize + VALUE_HEADER_SIZE);
            file.seek(headerOffset);
            file.writeLong(lookupPosition = fileSize);
            file.seek(lookupPosition);
            file.writeShort(adapter.typeId());
            file.writeInt(bufferSize);
            buffer.readBytes(file.getChannel(), bufferSize);
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
        long possibleId = id >> VALUE_ID_BITS;
        if (Long.compareUnsigned((possibleId | 0xFFFFFFFF), 0xFFFFFFFF) >= 1) {
            throw new StorageException("Unsupported file id '" + Long.toHexString(possibleId) + "'!");
        }
        int fileId = (int) (possibleId & 0xFFFFFFFF);
        short valueId = (short) (id & VALUE_ID_MASK);
        if (accesses.has(fileId)) {
            return delete(accesses.get(fileId), id, valueId);
        }
        RAFAccess<S> access = new RAFAccess<>(fileId, directory);
        if (!access.exists()) {
            return false;
        }
        accesses.set(fileId, access);
        return delete(access, id, valueId);
    }

    private boolean delete(RAFAccess<S> access, long fullId, short valueId) {
        access.writeLock();
        try {
            RandomAccessFile file = access.open();
            long fileSize = file.length();
            if (fileSize == 0) {
                accesses.remove(access.id());
                access.close();
                access.file().delete();
                return false;
            }
            long headerOffset = LOOKUP_AMOUNT_SIZE + LOOKUP_ENTRY_SIZE * valueId;
            file.seek(headerOffset);
            long lookupPosition = file.readLong();
            if(lookupPosition == INVALID_HEADER_OFFSET) {
                return false;
            }
            file.seek(lookupPosition);
            int bufferSize = file.readInt();
            if(deleteEntry(file, lookupPosition, bufferSize, headerOffset)) {
                accesses.remove(access.id());
                access.close();
                access.file().delete();
            }
            return true;
        } catch (IOException e) {
            throw new StorageException("Failed to delete value with id '" + Long.toHexString(fullId) + "' from file!", e);
        } finally {
            access.writeUnlock();
        }
    }
    
    private boolean deleteEntry(RandomAccessFile file, long lookupPosition, int bufferSize, long headerOffset) throws IOException {
        file.seek(0);
        short amount = file.readShort();
        if(amount - 1 == 0) {
            return true;
        }
        file.seek(0);
        file.writeShort(amount - 1);
        file.seek(headerOffset);
        file.writeLong(INVALID_HEADER_OFFSET);
        long offset = updateFileSize(file, lookupPosition, bufferSize, 0);
        long newDataEnd = lookupPosition + bufferSize + VALUE_HEADER_SIZE;
        if (offset != 0) {
            file.seek(LOOKUP_AMOUNT_SIZE);
            while (file.getFilePointer() != LOOKUP_HEADER_SIZE) {
                long entryOffset = file.readLong();
                if (entryOffset < newDataEnd) {
                    continue;
                }
                file.seek(file.getFilePointer() - LOOKUP_ENTRY_SIZE);
                file.writeLong(entryOffset + offset);
            }
        }
        return false;
    }

    /*
     * File size management
     */

    private long updateFileSize(RandomAccessFile file, long offset, long oldSize, long newSize) throws IOException {
        long difference = newSize - oldSize;
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
        byte[] buffer = new byte[COPY_BUFFER_SIZE];
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
        byte[] buffer = new byte[COPY_BUFFER_SIZE];
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
     * Update each data entry
     */

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

}
