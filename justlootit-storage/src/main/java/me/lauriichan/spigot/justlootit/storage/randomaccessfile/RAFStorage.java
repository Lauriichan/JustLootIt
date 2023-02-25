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

    private static final int LOOKUP_HEADER_SIZE = Long.BYTES;

    private static final int LOOKUP_ENTRY_ID_SIZE = Short.BYTES;
    private static final int LOOKUP_ENTRY_OFFSET_SIZE = Long.BYTES;
    private static final int LOOKUP_ENTRY_SIZE = LOOKUP_ENTRY_ID_SIZE + LOOKUP_ENTRY_OFFSET_SIZE;

    private static final int VALUE_HEADER_ID_SIZE = Short.BYTES;
    private static final int VALUE_HEADER_LENGTH_SIZE = Integer.BYTES;
    private static final int VALUE_HEADER_SIZE = VALUE_HEADER_ID_SIZE + VALUE_HEADER_LENGTH_SIZE;

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
        long possibleId = id >> 10;
        if (Long.compareUnsigned((possibleId | 0xFFFFFFFF), 0xFFFFFFFF) >= 1) {
            throw new StorageException("Unsupported file id '" + Long.toHexString(possibleId) + "'!");
        }
        int fileId = (int) (possibleId & 0xFFFFFFFF);
        short valueId = (short) (id & 0x3FF);
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
                // Remove file access as its empty
                accesses.remove(access.id());
                return null;
            }
            long offsetPosition = fileSize - LOOKUP_HEADER_SIZE;
            file.seek(offsetPosition);
            long headerPosition = file.readLong();
            file.seek(headerPosition);
            long lookupPosition = offsetPosition;
            long lookupHeaderPosition = 0;
            while (file.getFilePointer() != offsetPosition) {
                if (file.readShort() != valueId) {
                    file.skipBytes(LOOKUP_ENTRY_OFFSET_SIZE);
                    continue;
                }
                lookupHeaderPosition = file.getFilePointer() - LOOKUP_ENTRY_ID_SIZE;
                lookupPosition = file.readLong();
            }
            if (lookupPosition == offsetPosition) {
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
                    if(deleteEntry(file, lookupPosition, bufferSize, headerPosition, lookupHeaderPosition, offsetPosition)) {
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
            if (fileSize == 0) {
                int bufferSize = buffer.readableBytes();
                file.setLength(bufferSize + LOOKUP_HEADER_SIZE + LOOKUP_ENTRY_SIZE + VALUE_HEADER_SIZE);
                fileSize = file.length();
                long headerOffset = fileSize - LOOKUP_HEADER_SIZE;
                file.seek(headerOffset);
                file.writeLong(headerOffset -= LOOKUP_ENTRY_SIZE);
                file.seek(headerOffset);
                file.writeShort(valueId);
                file.writeLong(0L);
                file.seek(0);
                file.writeShort(adapter.typeId());
                file.writeInt(bufferSize);
                buffer.readBytes(file.getChannel(), bufferSize);
                return;
            }
            long offsetPosition = file.length() - LOOKUP_HEADER_SIZE;
            file.seek(offsetPosition);
            long headerPosition = file.readLong();
            file.seek(headerPosition);
            long lookupPosition = offsetPosition;
            while (file.getFilePointer() != offsetPosition) {
                if (file.readShort() != valueId) {
                    file.skipBytes(LOOKUP_ENTRY_OFFSET_SIZE);
                    continue;
                }
                lookupPosition = file.readLong();
            }
            int bufferSize = buffer.readableBytes();
            if (lookupPosition != offsetPosition) {
                file.seek(lookupPosition);
                long dataOffset = file.readLong();
                file.seek(dataOffset);
                int dataSize = file.readInt();
                long offset = updateFileSize(file, dataOffset, dataSize + VALUE_HEADER_SIZE, bufferSize + VALUE_HEADER_SIZE);
                file.seek(offsetPosition + offset);
                long newHeaderPosition = headerPosition + offset;
                file.writeLong(newHeaderPosition);
                if (offset != 0) {
                    file.seek(newHeaderPosition);
                    while (file.getFilePointer() != offsetPosition) {
                        file.skipBytes(LOOKUP_ENTRY_ID_SIZE);
                        long entryOffset = file.readLong();
                        if (entryOffset < dataOffset) {
                            continue;
                        }
                        file.seek(file.getFilePointer() - LOOKUP_ENTRY_OFFSET_SIZE);
                        file.writeLong(entryOffset + offset);
                    }
                }
                file.seek(dataOffset);
                file.writeShort(adapter.typeId());
                file.writeInt(bufferSize);
                buffer.readBytes(file.getChannel(), bufferSize);
                return;
            }
            long offset = updateFileSize(file, headerPosition, 0, bufferSize + VALUE_HEADER_SIZE + LOOKUP_ENTRY_SIZE);
            offsetPosition += offset;
            file.seek(offsetPosition);
            long newHeaderPosition = headerPosition + offset - LOOKUP_ENTRY_SIZE;
            file.writeLong(newHeaderPosition);
            file.seek(newHeaderPosition);
            file.writeShort(valueId);
            file.writeLong(headerPosition);
            file.seek(headerPosition);
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
        long possibleId = id >> 10;
        if (Long.compareUnsigned((possibleId | 0xFFFFFFFF), 0xFFFFFFFF) >= 1) {
            throw new StorageException("Unsupported file id '" + Long.toHexString(possibleId) + "'!");
        }
        int fileId = (int) (possibleId & 0xFFFFFFFF);
        short valueId = (short) (id & 0x3FF);
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
                // Remove file access as its empty
                accesses.remove(access.id());
                return false;
            }
            long offsetPosition = fileSize - LOOKUP_HEADER_SIZE;
            file.seek(offsetPosition);
            long headerPosition = file.readLong();
            file.seek(headerPosition);
            long lookupPosition = offsetPosition;
            long lookupHeaderPosition = 0;
            while (file.getFilePointer() != offsetPosition) {
                if (file.readShort() != valueId) {
                    file.skipBytes(LOOKUP_ENTRY_OFFSET_SIZE);
                    continue;
                }
                lookupHeaderPosition = file.getFilePointer() - LOOKUP_ENTRY_ID_SIZE;
                lookupPosition = file.readLong();
            }
            if (lookupPosition == offsetPosition) {
                return false;
            }
            file.seek(lookupPosition + VALUE_HEADER_ID_SIZE);
            int bufferSize = file.readInt();
            if(deleteEntry(file, lookupPosition, bufferSize, headerPosition, lookupHeaderPosition, offsetPosition)) {
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
    
    private boolean deleteEntry(RandomAccessFile file, long lookupPosition, int bufferSize, long headerPosition, long lookupHeaderPosition, long offsetPosition) throws IOException {
        long offset = updateFileSize(file, lookupPosition, bufferSize, 0);
        offset += updateFileSize(file, lookupHeaderPosition + offset, LOOKUP_ENTRY_SIZE, 0);
        offsetPosition = file.length() - LOOKUP_HEADER_SIZE;
        if (offsetPosition == 0) {
            return true;
        }
        file.seek(offsetPosition);
        long newHeaderPosition = headerPosition + offset;
        file.writeLong(newHeaderPosition);
        if (offset != 0) {
            lookupPosition += bufferSize + VALUE_HEADER_LENGTH_SIZE;
            file.seek(newHeaderPosition);
            while (file.getFilePointer() != offsetPosition) {
                file.skipBytes(LOOKUP_ENTRY_ID_SIZE);
                long entryOffset = file.readLong();
                if (entryOffset < lookupPosition) {
                    continue;
                }
                file.seek(file.getFilePointer() - LOOKUP_ENTRY_OFFSET_SIZE);
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
