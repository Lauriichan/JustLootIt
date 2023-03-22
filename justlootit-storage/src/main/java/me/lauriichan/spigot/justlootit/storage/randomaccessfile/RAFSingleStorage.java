package me.lauriichan.spigot.justlootit.storage.randomaccessfile;

import static me.lauriichan.spigot.justlootit.storage.randomaccessfile.RAFSettings.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;
import java.util.function.Function;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.shorts.Short2LongOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.storage.AbstractStorage;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;
import me.lauriichan.spigot.justlootit.storage.StorageException;
import me.lauriichan.spigot.justlootit.storage.UpdateInfo;
import me.lauriichan.spigot.justlootit.storage.UpdateInfo.UpdateState;
import me.lauriichan.spigot.justlootit.storage.util.cache.ThreadSafeSingletonCache;

public class RAFSingleStorage<S extends Storable> extends AbstractStorage<S> {

    private final RAFSettings settings;

    private final File file;
    private final ThreadSafeSingletonCache<RAFAccess<S>> accessCache;

    public RAFSingleStorage(ISimpleLogger logger, Class<S> baseType, File file) {
        this(logger, baseType, file, RAFSettings.DEFAULT);
    }

    public RAFSingleStorage(ISimpleLogger logger, Class<S> baseType, File file, RAFSettings settings) {
        super(logger, baseType);
        this.file = file;
        this.settings = settings;
        this.accessCache = new ThreadSafeSingletonCache<>(logger);
    }
    
    @Override
    public boolean isSupported(long id) {
        return id < settings.valueIdAmount && id >= 0;
    }

    /*
     * Clear data
     */

    @Override
    public void clear() {
        RAFAccess<S> access = accessCache.remove();
        if (access == null) {
            return;
        }
        if (access.isOpen()) {
            access.writeLock();
            try {
                access.close();
            } catch (Exception e) {
                logger.warning("Couldn't close File access to '" + access.hexId() + "'");
            } finally {
                access.writeUnlock();
            }
        }
        access.file().delete();
    }

    @Override
    public void close() {
        RAFAccess<S> access = accessCache.remove();
        if (access == null || !access.isOpen()) {
            return;
        }
        access.writeLock();
        try {
            access.close();
        } catch (Exception e) {
            logger.warning("Couldn't close File access to '" + access.hexId() + "'");
        } finally {
            access.writeUnlock();
        }
    }

    /*
     * Data reading
     */
    
    @Override
    public boolean has(long id) throws StorageException {
        if (id >= settings.valueIdAmount || id < 0) {
            return false;
        }
        short valueId = (short) (id & settings.valueIdMask);
        if (accessCache.isPresent()) {
            return has(accessCache.get(), id, valueId);
        }
        RAFAccess<S> access = new RAFAccess<>(file);
        if (!access.exists()) {
            try {
                access.close();
            } catch (IOException e) {
                // Ignore because we're not even open
            }
            return false;
        }
        accessCache.set(access);
        return has(access, id, valueId);
    }
    
    private boolean has(RAFAccess<S> access, long fullId, short valueId) {
        access.readLock();
        try {
            RandomAccessFile file = access.open();
            long fileSize = file.length();
            if (fileSize == 0) {
                accessCache.remove();
                access.close();
                access.file().delete();
                return false;
            }
            long headerOffset = LOOKUP_AMOUNT_SIZE + LOOKUP_ENTRY_SIZE * valueId;
            file.seek(headerOffset);
            long lookupPosition = file.readLong();
            access.readUnlock();
            return lookupPosition != INVALID_HEADER_OFFSET;
        } catch (IOException e) {
            access.readUnlock();
            throw new StorageException("Failed to check if value with id '" + Long.toHexString(fullId) + "' exists!", e);
        }
    }
    

    @Override
    public S read(long id) throws StorageException {
        if (id >= settings.valueIdAmount || id < 0) {
            throw new StorageException("Unsupported value id '" + id + "'!");
        }
        short valueId = (short) (id & settings.valueIdMask);
        if (accessCache.isPresent()) {
            return read(accessCache.get(), id, valueId);
        }
        RAFAccess<S> access = new RAFAccess<>(file);
        if (!access.exists()) {
            try {
                access.close();
            } catch (IOException e) {
                // Ignore because we're not even open
            }
            return null;
        }
        accessCache.set(access);
        return read(access, id, valueId);
    }

    private S read(RAFAccess<S> access, long fullId, short valueId) {
        access.readLock();
        try {
            RandomAccessFile file = access.open();
            long fileSize = file.length();
            if (fileSize == 0) {
                accessCache.remove();
                access.close();
                access.file().delete();
                return null;
            }
            long headerOffset = LOOKUP_AMOUNT_SIZE + LOOKUP_ENTRY_SIZE * valueId;
            file.seek(headerOffset);
            long lookupPosition = file.readLong();
            if (lookupPosition == INVALID_HEADER_OFFSET) {
                access.readUnlock();
                return null;
            }
            file.seek(lookupPosition);
            short typeId = file.readShort();
            int dataSize = file.readInt();
            StorageAdapter<? extends S> adapter = findAdapterFor(typeId);
            if (adapter == null) {
                access.readUnlock();
                access.writeLock();
                try {
                    if (deleteEntry(file, lookupPosition, dataSize, headerOffset)) {
                        accessCache.remove();
                        access.close();
                        access.file().delete();
                    }
                } catch (IOException e) {
                    throw new StorageException("Failed to delete value with id '" + Long.toHexString(fullId) + "', because of the type "
                        + typeId + " is unknown, from file!", e);
                } finally {
                    access.writeUnlock();
                }
                throw new StorageException("Failed to read value with id '" + Long.toHexString(fullId) + "' from file because the type "
                    + typeId + " is unknown!");
            }
            byte[] rawBuffer = new byte[dataSize];
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
        if (id >= settings.valueIdAmount || id < 0) {
            throw new StorageException("Unsupported value id '" + id + "'!");
        }
        short valueId = (short) (id & settings.valueIdMask);
        if (accessCache.isPresent()) {
            write(accessCache.get(), valueId, storable);
            return;
        }
        RAFAccess<S> access = new RAFAccess<>(file);
        accessCache.set(access);
        write(access, valueId, storable);
    }

    private void write(RAFAccess<S> access, short valueId, S storable) throws StorageException {
        StorageAdapter<? extends S> adapter = findAdapterFor(storable.getClass().asSubclass(baseType));
        if (adapter == null) {
            throw new StorageException("Couldn't find storage adapter for type '" + storable.getClass().getName() + "'!");
        }
        ByteBuf buffer;
        try {
            buffer = adapter.serializeValue(storable);
        } catch (RuntimeException e) {
            throw new StorageException("Failed to write value with id '" + Long.toHexString(storable.id()) + "' to file!", e);
        }
        access.writeLock();
        try {
            RandomAccessFile file = access.open();
            long fileSize = file.length();
            if (fileSize == 0) {
                int bufferSize = buffer.readableBytes();
                file.setLength(settings.lookupHeaderSize + bufferSize);
                fileSize = file.length();
                file.seek(0);
                file.writeShort(1);
                file.skipBytes(LOOKUP_ENTRY_SIZE * valueId);
                file.writeLong(settings.lookupHeaderSize);
                file.seek(settings.lookupHeaderSize);
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
                long offset = updateFileSize(file, lookupPosition, dataSize, bufferSize);
                if (offset != 0) {
                    file.seek(LOOKUP_AMOUNT_SIZE);
                    long newDataEnd = lookupPosition + bufferSize + VALUE_HEADER_SIZE;
                    while (file.getFilePointer() != settings.lookupHeaderSize) {
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
        if (id >= settings.valueIdAmount || id < 0) {
            throw new StorageException("Unsupported value id '" + id + "'!");
        }
        short valueId = (short) (id & settings.valueIdMask);
        if (accessCache.isPresent()) {
            return delete(accessCache.get(), id, valueId);
        }
        RAFAccess<S> access = new RAFAccess<>(file);
        if (!access.exists()) {
            return false;
        }
        accessCache.set(access);
        return delete(access, id, valueId);
    }

    private boolean delete(RAFAccess<S> access, long fullId, short valueId) {
        access.writeLock();
        try {
            RandomAccessFile file = access.open();
            long fileSize = file.length();
            if (fileSize == 0) {
                accessCache.remove();
                access.close();
                access.file().delete();
                return false;
            }
            long headerOffset = LOOKUP_AMOUNT_SIZE + LOOKUP_ENTRY_SIZE * valueId;
            file.seek(headerOffset);
            long lookupPosition = file.readLong();
            if (lookupPosition == INVALID_HEADER_OFFSET) {
                return false;
            }
            file.seek(lookupPosition + VALUE_HEADER_ID_SIZE);
            int dataSize = file.readInt();
            if (deleteEntry(file, lookupPosition, dataSize, headerOffset)) {
                accessCache.remove();
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

    private boolean deleteEntry(RandomAccessFile file, long lookupPosition, int dataSize, long headerOffset) throws IOException {
        file.seek(0);
        short amount = file.readShort();
        if (amount - 1 == 0) {
            return true;
        }
        file.seek(0);
        file.writeShort(amount - 1);
        file.seek(headerOffset);
        file.writeLong(INVALID_HEADER_OFFSET);
        long offset = updateFileSize(file, lookupPosition, dataSize + VALUE_HEADER_SIZE, 0);
        if (offset != 0) {
            file.seek(LOOKUP_AMOUNT_SIZE);
            while (file.getFilePointer() != settings.lookupHeaderSize) {
                long entryOffset = file.readLong();
                if (entryOffset < lookupPosition) {
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
            shrinkFile(file, offset + newSize, difference * -1);
            return difference;
        }
        expandFile(file, offset + oldSize, difference);
        return difference;
    }

    private void expandFile(RandomAccessFile file, long offset, long amount) throws IOException {
        long oldFileEnd = file.length();
        file.setLength(oldFileEnd + amount);
        long pointer = oldFileEnd;
        byte[] buffer = new byte[settings.copyBufferSize];
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
        long oldLength = file.length();
        long newLength = oldLength - amount;
        byte[] buffer = new byte[settings.copyBufferSize];
        while (pointer != oldLength) {
            long diff = oldLength - pointer;
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
    public void updateEach(Function<S, UpdateInfo<S>> updater) {
        if (!file.exists()) {
            return;
        }
        accessCache.tickPaused(true);
        try {
            if (accessCache.isPresent()) {
                try {
                    doUpdate(accessCache.peek(), updater);
                } catch (IOException e) {
                    logger.warning("Failed to run update for file '" + file.getName() + "'!", e);
                }
                return;
            }
            try (RAFAccess<S> access = new RAFAccess<>(file)) {
                doUpdate(access, updater);
            } catch (IOException e) {
                logger.warning("Failed to run update for file '" + file.getName() + "'!", e);
            }
        } finally {
            accessCache.tickPaused(false);
        }
    }

    private void doUpdate(RAFAccess<S> access, Function<S, UpdateInfo<S>> updater) throws IOException {
        access.writeLock();
        try {
            RandomAccessFile file = access.open();
            long fileSize = file.length();
            if (fileSize == 0) {
                accessCache.remove();
                access.close();
                access.file().delete();
                return;
            }
            long idBase = access.id() << settings.valueIdBits;
            ShortArrayList delete = new ShortArrayList();
            file.seek(0);
            short items = file.readShort();
            long headerOffset;
            long lookupPosition;
            for (short valueId = 0; valueId < settings.valueIdAmount; valueId++) {
                headerOffset = LOOKUP_AMOUNT_SIZE + LOOKUP_ENTRY_SIZE * valueId;
                file.seek(headerOffset);
                lookupPosition = file.readLong();
                if (lookupPosition == INVALID_HEADER_OFFSET) {
                    continue;
                }
                long fullId = idBase + valueId;
                file.seek(lookupPosition);
                short typeId = file.readShort();
                int dataSize = file.readInt();
                StorageAdapter<? extends S> adapter = findAdapterFor(typeId);
                if (adapter == null) {
                    if ((items -= 1) == 0) {
                        accessCache.remove();
                        access.close();
                        access.file().delete();
                        return; // File is gone
                    }
                    delete.add(valueId);
                    continue;
                }
                byte[] rawBuffer = new byte[dataSize];
                file.read(rawBuffer);
                S storable = null;
                try {
                    storable = adapter.deserialize(fullId, Unpooled.wrappedBuffer(rawBuffer));
                } catch (IndexOutOfBoundsException exp) {
                    logger.warning("Couldn't deserialize resource with id '" + Long.toHexString(fullId) + "'!", exp);
                }
                rawBuffer = null; // We no longer need this data, this can be a lot so we remove it from cache
                if (storable == null) {
                    if ((items -= 1) == 0) {
                        accessCache.remove();
                        access.close();
                        access.file().delete();
                        return; // File is gone
                    }
                    delete.add(valueId);
                    continue;
                }
                UpdateInfo<S> info = UpdateInfo.none();
                try {
                    info = Objects.requireNonNull(updater.apply(storable), "Update state can't be null");
                } catch (Throwable exp) {
                    logger.warning("Couldn't update resource with id '" + Long.toHexString(fullId) + "'!", exp);
                    continue;
                }
                UpdateState state = info.state();
                if (state == UpdateState.NONE) {
                    continue;
                }
                if (state == UpdateState.DELETE) {
                    if ((items -= 1) == 0) {
                        accessCache.remove();
                        access.close();
                        access.file().delete();
                        return; // File is gone
                    }
                    delete.add(valueId);
                    continue;
                }
                if (info.storable() != null) {
                    storable = info.storable();
                    adapter = findAdapterFor(storable.getClass().asSubclass(baseType));
                    if (adapter == null) {
                        logger.warning("Couldn't update resource with id '" + Long.toHexString(fullId) + "' because the type '"
                            + storable.getClass().getName() + "' is unknown!");
                        continue;
                    }
                }
                ByteBuf buffer;
                try {
                    buffer = adapter.serializeValue(storable);
                } catch (RuntimeException exp) {
                    logger.warning("Couldn't update resource with id '" + Long.toHexString(fullId) + "'!", exp);
                    continue;
                }
                int bufferSize = buffer.readableBytes();
                long offset = updateFileSize(file, lookupPosition, dataSize, bufferSize);
                if (offset != 0) {
                    file.seek(LOOKUP_AMOUNT_SIZE);
                    long newDataEnd = lookupPosition + bufferSize + VALUE_HEADER_SIZE;
                    while (file.getFilePointer() != settings.lookupHeaderSize) {
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
            }
            int amount = delete.size();
            if (amount == 0) {
                return;
            }
            file.seek(0);
            file.writeShort(items);
            // Here we delete all entries mentioned above
            // This should speed up this process by a lot compared to individual delete operations
            long newFileSize = file.length();
            Long2IntOpenHashMap keysToIndex = new Long2IntOpenHashMap(items);
            LongArrayList headerKeys = new LongArrayList(items);
            LongArrayList headerValues = new LongArrayList(items);
            LongArrayList headerNewValues = new LongArrayList(items);
            int headerAmount = 0;
            Short2LongOpenHashMap deleteHeaders = new Short2LongOpenHashMap(amount);
            for (short valueId = 0; valueId < settings.valueIdAmount; valueId++) {
                headerOffset = LOOKUP_AMOUNT_SIZE + LOOKUP_ENTRY_SIZE * valueId;
                file.seek(headerOffset);
                lookupPosition = file.readLong();
                if (lookupPosition == INVALID_HEADER_OFFSET) {
                    continue;
                }
                if (delete.contains(valueId)) {
                    deleteHeaders.put(valueId, lookupPosition);
                    continue;
                }
                keysToIndex.put(headerOffset, headerAmount++);
                headerKeys.add(headerOffset);
                headerValues.add(lookupPosition);
                headerNewValues.add(lookupPosition);
            }
            int dataSize;
            while (amount != 0) {
                short valueId = delete.removeShort(0);
                amount--;
                headerOffset = LOOKUP_AMOUNT_SIZE + LOOKUP_ENTRY_SIZE * valueId;
                file.seek(headerOffset);
                file.writeLong(INVALID_HEADER_OFFSET);
                lookupPosition = deleteHeaders.remove(valueId);
                file.seek(lookupPosition + VALUE_HEADER_ID_SIZE);
                dataSize = file.readInt() + VALUE_HEADER_SIZE;
                newFileSize -= dataSize;
                for (int headerIdx = 0; headerIdx < items; headerIdx++) {
                    long headerValue = headerNewValues.getLong(headerIdx);
                    if (headerValue < lookupPosition) {
                        continue;
                    }
                    headerNewValues.set(headerIdx, headerValue - dataSize);
                }
                for (int entry = 0; entry < amount; entry++) {
                    short entryId = delete.getShort(entry);
                    long headerValue = deleteHeaders.get(entryId);
                    if (headerValue < lookupPosition) {
                        continue;
                    }
                    deleteHeaders.put(entryId, headerValue - dataSize);
                }
            }
            headerKeys
                .sort((k1, k2) -> Long.compare(headerNewValues.getLong(keysToIndex.get(k1)), headerNewValues.getLong(keysToIndex.get(k2))));
            int valueIdx;
            long copyFrom, copyTo, copyEnd, copyAmount;
            byte[] buffer = new byte[settings.copyBufferSize];
            for (int keyIdx = 0; keyIdx < headerAmount; keyIdx++) {
                headerOffset = headerKeys.getLong(keyIdx);
                valueIdx = keysToIndex.get(headerOffset);
                copyFrom = headerValues.getLong(valueIdx);
                copyTo = headerNewValues.getLong(valueIdx);
                if (copyTo == copyFrom) {
                    continue;
                }
                file.seek(headerOffset);
                file.writeLong(copyTo);
                file.seek(copyFrom + VALUE_HEADER_ID_SIZE);
                dataSize = file.readInt() + VALUE_HEADER_SIZE;
                file.seek(copyFrom);
                lookupPosition = copyFrom;
                copyEnd = copyFrom + dataSize;
                copyAmount = 0;
                while (lookupPosition != copyEnd) {
                    long diff = copyEnd - lookupPosition;
                    int size = diff > buffer.length ? buffer.length : (int) diff;
                    file.seek(lookupPosition);
                    file.read(buffer, 0, size);
                    file.seek(copyTo + copyAmount);
                    file.write(buffer, 0, size);
                    lookupPosition += size;
                    copyAmount += size;
                }
            }
            file.setLength(newFileSize);
        } finally {
            access.writeUnlock();
        }
    }

}
