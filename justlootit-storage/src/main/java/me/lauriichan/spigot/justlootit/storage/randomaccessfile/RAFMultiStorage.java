package me.lauriichan.spigot.justlootit.storage.randomaccessfile;

import static me.lauriichan.spigot.justlootit.storage.randomaccessfile.RAFSettings.INVALID_HEADER_OFFSET;
import static me.lauriichan.spigot.justlootit.storage.randomaccessfile.RAFSettings.LOOKUP_AMOUNT_SIZE;
import static me.lauriichan.spigot.justlootit.storage.randomaccessfile.RAFSettings.LOOKUP_ENTRY_SIZE;
import static me.lauriichan.spigot.justlootit.storage.randomaccessfile.RAFSettings.VALUE_HEADER_ID_SIZE;
import static me.lauriichan.spigot.justlootit.storage.randomaccessfile.RAFSettings.VALUE_HEADER_SIZE;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.shorts.Short2LongOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.storage.AbstractStorage;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;
import me.lauriichan.spigot.justlootit.storage.StorageException;
import me.lauriichan.spigot.justlootit.storage.UpdateInfo;
import me.lauriichan.spigot.justlootit.storage.UpdateInfo.UpdateState;
import me.lauriichan.spigot.justlootit.storage.identifier.FileIdentifier;
import me.lauriichan.spigot.justlootit.storage.identifier.IIdentifier;
import me.lauriichan.spigot.justlootit.storage.util.cache.Int2ObjectMapCache;
import me.lauriichan.spigot.justlootit.storage.util.cache.ThreadSafeMapCache;

public class RAFMultiStorage<S extends Storable> extends AbstractStorage<S> {

    private final RAFSettings settings;

    private final File directory;
    private final ThreadSafeMapCache<Integer, RAFAccess<S>> accesses;

    private final IIdentifier identifier;

    public RAFMultiStorage(final ISimpleLogger logger, final Class<S> baseType, final File directory) {
        this(logger, baseType, directory, RAFSettings.DEFAULT);
    }

    public RAFMultiStorage(final ISimpleLogger logger, final Class<S> baseType, final File directory, final RAFSettings settings) {
        super(logger, baseType);
        this.settings = settings;
        this.accesses = new ThreadSafeMapCache<>(new Int2ObjectMapCache<>(logger));
        this.directory = directory;
        createDirectory();
        this.identifier = new FileIdentifier(logger, directory);
    }
    
    private void createDirectory() {
        if (!directory.exists()) {
            directory.mkdirs();
        } else if(directory.isFile()) {
            directory.delete();
            directory.mkdirs();
        }
    }

    @Override
    public boolean isSupported(final long id) {
        return Long.compareUnsigned(id >> settings.valueIdBits | 0xFFFFFFFF, 0xFFFFFFFF) <= 0;
    }

    @Override
    public long newId() {
        long id = identifier.nextId();
        while (has(id)) {
            id = identifier.nextId();
        }
        return id;
    }

    /*
     * File cache
     */

    private void saveAccess(final RAFAccess<S> access) {
        if (accesses.size() < settings.fileCacheMaxAmount) {
            accesses.set(access.id(), access);
            return;
        }
        long cacheTime = settings.fileCacheTicks;
        while (accesses.size() >= settings.fileCacheMaxAmount) {
            cacheTime -= settings.fileCachePurgeStep;
            accesses.purge(cacheTime);
        }
        accesses.set(access.id(), access);
    }

    /*
     * Clear data
     */

    @Override
    public void clear() {
        final List<Integer> list = accesses.keys();
        for (final Integer id : list) {
            final RAFAccess<S> access = accesses.remove(id);
            if (access == null) {
                continue;
            }
            if (access.isOpen()) {
                access.lock();
                try {
                    access.close();
                } catch (final Exception e) {
                    logger.warning("Couldn't close File access to '" + access.hexId() + "'");
                } finally {
                    access.unlock();
                }
            }
            access.file().delete();
        }
        identifier.reset();
        identifier.save();
    }

    @Override
    public void close() {
        final List<Integer> list = accesses.keys();
        for (final Integer id : list) {
            final RAFAccess<S> access = accesses.remove(id);
            if (access == null || !access.isOpen()) {
                continue;
            }
            access.lock();
            try {
                access.close();
            } catch (final Exception e) {
                logger.warning("Couldn't close File access to '" + access.hexId() + "'");
            } finally {
                access.unlock();
            }
        }
        identifier.save();
    }

    /*
     * Data reading
     */

    @Override
    public boolean has(final long id) throws StorageException {
        final long possibleId = id >> settings.valueIdBits;
        if (Long.compareUnsigned(possibleId | 0xFFFFFFFF, 0xFFFFFFFF) >= 1) {
            return false;
        }
        final int fileId = (int) (possibleId & 0xFFFFFFFF);
        final short valueId = (short) (id & settings.valueIdMask);
        if (accesses.has(fileId)) {
            return has(accesses.get(fileId), id, valueId);
        }
        final RAFAccess<S> access = new RAFAccess<>(fileId, directory);
        if (!access.exists()) {
            try {
                access.close();
            } catch (final IOException e) {
                // Ignore because we're not even open
            }
            return false;
        }
        saveAccess(access);
        return has(access, id, valueId);
    }

    private boolean has(final RAFAccess<S> access, final long fullId, final short valueId) {
        access.lock();
        try {
            final RandomAccessFile file = access.open();
            final long fileSize = file.length();
            if (fileSize == 0) {
                accesses.remove(access.id());
                access.close();
                access.file().delete();
                return false;
            }
            final long headerOffset = LOOKUP_AMOUNT_SIZE + LOOKUP_ENTRY_SIZE * valueId;
            file.seek(headerOffset);
            final long lookupPosition = file.readLong();
            return lookupPosition != INVALID_HEADER_OFFSET;
        } catch (final IOException e) {
            throw new StorageException("Failed to check if value with id '" + Long.toHexString(fullId) + "' exists!", e);
        } finally {
            access.unlock();
        }
    }

    @Override
    public S read(final long id) throws StorageException {
        final long possibleId = id >> settings.valueIdBits;
        if (Long.compareUnsigned(possibleId | 0xFFFFFFFF, 0xFFFFFFFF) >= 1) {
            throw new StorageException("Unsupported file id '" + Long.toHexString(possibleId) + "'!");
        }
        final int fileId = (int) (possibleId & 0xFFFFFFFF);
        final short valueId = (short) (id & settings.valueIdMask);
        if (accesses.has(fileId)) {
            return read(accesses.get(fileId), id, valueId);
        }
        final RAFAccess<S> access = new RAFAccess<>(fileId, directory);
        if (!access.exists()) {
            try {
                access.close();
            } catch (final IOException e) {
                // Ignore because we're not even open
            }
            return null;
        }
        saveAccess(access);
        return read(access, id, valueId);
    }

    private S read(final RAFAccess<S> access, final long fullId, final short valueId) {
        access.lock();
        try {
            final RandomAccessFile file = access.open();
            final long fileSize = file.length();
            if (fileSize == 0) {
                accesses.remove(access.id());
                access.close();
                access.file().delete();
                return null;
            }
            final long headerOffset = LOOKUP_AMOUNT_SIZE + LOOKUP_ENTRY_SIZE * valueId;
            file.seek(headerOffset);
            final long lookupPosition = file.readLong();
            if (lookupPosition == INVALID_HEADER_OFFSET) {
                return null;
            }
            file.seek(lookupPosition);
            final short typeId = file.readShort();
            final int dataSize = file.readInt();
            final StorageAdapter<? extends S> adapter = findAdapterFor(typeId);
            if (adapter == null) {
                try {
                    if (deleteEntry(file, lookupPosition, dataSize, headerOffset)) {
                        accesses.remove(access.id());
                        access.close();
                        access.file().delete();
                    }
                } catch (final IOException e) {
                    throw new StorageException("Failed to delete value with id '" + Long.toHexString(fullId) + "', because of the type "
                        + typeId + " is unknown, from file!", e);
                }
                throw new StorageException("Failed to read value with id '" + Long.toHexString(fullId) + "' from file because the type "
                    + typeId + " is unknown!");
            }
            final byte[] rawBuffer = new byte[dataSize];
            file.read(rawBuffer);
            return adapter.deserialize(fullId, Unpooled.wrappedBuffer(rawBuffer));
        } catch (final IOException e) {
            throw new StorageException("Failed to read value with id '" + Long.toHexString(fullId) + "' from file!", e);
        } finally {
            access.unlock();
        }
    }

    /*
     * Data writing & file creation
     */

    @Override
    public void write(final S storable) throws StorageException {
        final long id = storable.id();
        final long possibleId = id >> settings.valueIdBits;
        if (Long.compareUnsigned(possibleId | 0xFFFFFFFF, 0xFFFFFFFF) >= 1) {
            throw new StorageException("Unsupported file id '" + Long.toHexString(possibleId) + "'!");
        }
        final int fileId = (int) (possibleId & 0xFFFFFFFF);
        final short valueId = (short) (id & settings.valueIdMask);
        if (accesses.has(fileId)) {
            write(accesses.get(fileId), valueId, storable);
            return;
        }
        final RAFAccess<S> access = new RAFAccess<>(fileId, directory);
        saveAccess(access);
        write(access, valueId, storable);
    }

    private void write(final RAFAccess<S> access, final short valueId, final S storable) throws StorageException {
        final StorageAdapter<? extends S> adapter = findAdapterFor(storable.getClass().asSubclass(baseType));
        if (adapter == null) {
            throw new StorageException("Couldn't find storage adapter for type '" + storable.getClass().getName() + "'!");
        }
        ByteBuf buffer;
        try {
            buffer = adapter.serializeValue(storable);
        } catch (final RuntimeException e) {
            throw new StorageException("Failed to write value with id '" + Long.toHexString(storable.id()) + "' to file!", e);
        }
        access.lock();
        try {
            final RandomAccessFile file = access.open();
            long fileSize = file.length();
            if (fileSize == 0) {
                final int bufferSize = buffer.readableBytes();
                file.setLength(settings.lookupHeaderSize + bufferSize);
                fileSize = file.length();
                file.seek(0);
                file.writeShort(1);
                file.skipBytes(LOOKUP_ENTRY_SIZE * valueId);
                file.writeLong(settings.lookupHeaderSize);
                file.seek(settings.lookupHeaderSize);
                file.writeShort(adapter.typeId());
                file.writeInt(bufferSize);
                buffer.readBytes(file.getChannel(), file.getFilePointer(), bufferSize);
                return;
            }
            final long headerOffset = LOOKUP_AMOUNT_SIZE + LOOKUP_ENTRY_SIZE * valueId;
            file.seek(headerOffset);
            long lookupPosition = file.readLong();
            buffer.resetReaderIndex();
            final int bufferSize = buffer.readableBytes();
            if (lookupPosition != INVALID_HEADER_OFFSET) {
                file.seek(lookupPosition + VALUE_HEADER_ID_SIZE);
                final int dataSize = file.readInt();
                final long offset = updateFileSize(file, lookupPosition, dataSize, bufferSize);
                if (offset != 0) {
                    file.seek(LOOKUP_AMOUNT_SIZE);
                    final long oldDataEnd = lookupPosition + dataSize + VALUE_HEADER_SIZE;
                    while (file.getFilePointer() != settings.lookupHeaderSize) {
                        final long entryOffset = file.readLong();
                        if (entryOffset < oldDataEnd) {
                            continue;
                        }
                        file.seek(file.getFilePointer() - LOOKUP_ENTRY_SIZE);
                        file.writeLong(entryOffset + offset);
                    }
                }
                file.seek(lookupPosition);
                file.writeShort(adapter.typeId());
                file.writeInt(bufferSize);
                buffer.readBytes(file.getChannel(), file.getFilePointer(), bufferSize);
                return;
            }
            file.seek(0);
            final short amount = file.readShort();
            file.seek(0);
            file.writeShort(amount + 1);
            file.setLength(fileSize + bufferSize + VALUE_HEADER_SIZE);
            file.seek(headerOffset);
            file.writeLong(lookupPosition = fileSize);
            file.seek(lookupPosition);
            file.writeShort(adapter.typeId());
            file.writeInt(bufferSize);
            buffer.readBytes(file.getChannel(), file.getFilePointer(), bufferSize);
        } catch (final IOException e) {
            throw new StorageException("Failed to write value with id '" + Long.toHexString(storable.id()) + "' to file!", e);
        } finally {
            access.unlock();
        }
    }

    /*
     * Data deletion & file deletion
     */

    @SuppressWarnings("resource")
    @Override
    public boolean delete(final long id) throws StorageException {
        final long possibleId = id >> settings.valueIdBits;
        if (Long.compareUnsigned(possibleId | 0xFFFFFFFF, 0xFFFFFFFF) >= 1) {
            throw new StorageException("Unsupported file id '" + Long.toHexString(possibleId) + "'!");
        }
        final int fileId = (int) (possibleId & 0xFFFFFFFF);
        final short valueId = (short) (id & settings.valueIdMask);
        if (accesses.has(fileId)) {
            return delete(accesses.get(fileId), id, valueId);
        }
        final RAFAccess<S> access = new RAFAccess<>(fileId, directory);
        if (!access.exists()) {
            return false;
        }
        saveAccess(access);
        return delete(access, id, valueId);
    }

    private boolean delete(final RAFAccess<S> access, final long fullId, final short valueId) {
        access.lock();
        try {
            final RandomAccessFile file = access.open();
            final long fileSize = file.length();
            if (fileSize == 0) {
                accesses.remove(access.id());
                access.close();
                access.file().delete();
                return false;
            }
            final long headerOffset = LOOKUP_AMOUNT_SIZE + LOOKUP_ENTRY_SIZE * valueId;
            file.seek(headerOffset);
            final long lookupPosition = file.readLong();
            if (lookupPosition == INVALID_HEADER_OFFSET) {
                return false;
            }
            file.seek(lookupPosition + VALUE_HEADER_ID_SIZE);
            final int dataSize = file.readInt();
            if (deleteEntry(file, lookupPosition, dataSize, headerOffset)) {
                accesses.remove(access.id());
                access.close();
                access.file().delete();
            }
            return true;
        } catch (final IOException e) {
            throw new StorageException("Failed to delete value with id '" + Long.toHexString(fullId) + "' from file!", e);
        } finally {
            access.unlock();
        }
    }

    private boolean deleteEntry(final RandomAccessFile file, final long lookupPosition, final int dataSize, final long headerOffset)
        throws IOException {
        file.seek(0);
        final short amount = file.readShort();
        if (amount - 1 == 0) {
            return true;
        }
        file.seek(0);
        file.writeShort(amount - 1);
        file.seek(headerOffset);
        file.writeLong(INVALID_HEADER_OFFSET);
        final long offset = updateFileSize(file, lookupPosition, dataSize + VALUE_HEADER_SIZE, 0);
        if (offset != 0) {
            file.seek(LOOKUP_AMOUNT_SIZE);
            while (file.getFilePointer() != settings.lookupHeaderSize) {
                final long entryOffset = file.readLong();
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

    private long updateFileSize(final RandomAccessFile file, final long offset, final long oldSize, final long newSize) throws IOException {
        final long difference = newSize - oldSize;
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

    private void expandFile(final RandomAccessFile file, final long offset, final long amount) throws IOException {
        final long oldFileEnd = file.length();
        file.setLength(oldFileEnd + amount);
        long pointer = oldFileEnd;
        final byte[] buffer = new byte[settings.copyBufferSize];
        while (pointer != offset) {
            final long diff = pointer - offset;
            final int size = diff > buffer.length ? buffer.length : (int) diff;
            pointer -= size;
            file.seek(pointer);
            file.read(buffer, 0, size);
            file.seek(pointer + amount);
            file.write(buffer, 0, size);
        }
    }

    private void shrinkFile(final RandomAccessFile file, final long offset, final long amount) throws IOException {
        long pointer = offset + amount;
        final long oldLength = file.length();
        final long newLength = oldLength - amount;
        final byte[] buffer = new byte[settings.copyBufferSize];
        while (pointer != oldLength) {
            final long diff = oldLength - pointer;
            final int size = diff > buffer.length ? buffer.length : (int) diff;
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
    public void updateEach(final Function<S, UpdateInfo<S>> updater) {
        if (!directory.exists()) {
            return;
        }
        accesses.tickPaused(true);
        final File[] files = directory.listFiles(RAFAccess.FILE_FILTER);
        for (final File file : files) {
            int fileId;
            try {
                fileId = Integer.parseInt(file.getName().substring(0, file.getName().length() - 4));
            } catch (final NumberFormatException nfe) {
                continue;
            }
            if (accesses.has(fileId)) {
                try {
                    doUpdate(accesses.peek(fileId), updater);
                } catch (final IOException e) {
                    logger.warning("Failed to run update for file '" + Integer.toHexString(fileId) + "'!", e);
                }
                continue;
            }
            try (RAFAccess<S> access = new RAFAccess<>(fileId, directory)) {
                doUpdate(access, updater);
            } catch (final IOException e) {
                logger.warning("Failed to run update for file '" + Integer.toHexString(fileId) + "'!", e);
            }
        }
        accesses.tickPaused(false);
    }

    private void doUpdate(final RAFAccess<S> access, final Function<S, UpdateInfo<S>> updater) throws IOException {
        access.lock();
        try {
            final RandomAccessFile file = access.open();
            final long fileSize = file.length();
            if (fileSize == 0) {
                if (accesses.has(access.id())) {
                    accesses.remove(access.id());
                }
                access.close();
                access.file().delete();
                return;
            }
            final long idBase = access.id() << settings.valueIdBits;
            final ShortArrayList delete = new ShortArrayList();
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
                final long fullId = idBase + valueId;
                file.seek(lookupPosition);
                final short typeId = file.readShort();
                final int dataSize = file.readInt();
                StorageAdapter<? extends S> adapter = findAdapterFor(typeId);
                if (adapter == null) {
                    if ((items -= 1) == 0) {
                        accesses.remove(access.id());
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
                } catch (final IndexOutOfBoundsException exp) {
                    logger.warning("Couldn't deserialize resource with id '" + Long.toHexString(fullId) + "'!", exp);
                }
                rawBuffer = null; // We no longer need this data, this can be a lot so we remove it from cache
                if (storable == null) {
                    if ((items -= 1) == 0) {
                        accesses.remove(access.id());
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
                } catch (final Throwable exp) {
                    logger.warning("Couldn't update resource with id '" + Long.toHexString(fullId) + "'!", exp);
                    continue;
                }
                final UpdateState state = info.state();
                if (state == UpdateState.NONE) {
                    continue;
                }
                if (state == UpdateState.DELETE) {
                    if ((items -= 1) == 0) {
                        accesses.remove(access.id());
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
                } catch (final RuntimeException exp) {
                    logger.warning("Couldn't update resource with id '" + Long.toHexString(fullId) + "'!", exp);
                    continue;
                }
                final int bufferSize = buffer.readableBytes();
                final long offset = updateFileSize(file, lookupPosition, dataSize, bufferSize);
                if (offset != 0) {
                    file.seek(LOOKUP_AMOUNT_SIZE);
                    final long oldDataEnd = lookupPosition + dataSize + VALUE_HEADER_SIZE;
                    while (file.getFilePointer() != settings.lookupHeaderSize) {
                        final long entryOffset = file.readLong();
                        if (entryOffset < oldDataEnd) {
                            continue;
                        }
                        file.seek(file.getFilePointer() - LOOKUP_ENTRY_SIZE);
                        file.writeLong(entryOffset + offset);
                    }
                }
                file.seek(lookupPosition);
                file.writeShort(adapter.typeId());
                file.writeInt(bufferSize);
                buffer.readBytes(file.getChannel(), file.getFilePointer(), bufferSize);
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
            final Long2IntOpenHashMap keysToIndex = new Long2IntOpenHashMap(items);
            final LongArrayList headerKeys = new LongArrayList(items);
            final LongArrayList headerValues = new LongArrayList(items);
            final LongArrayList headerNewValues = new LongArrayList(items);
            int headerAmount = 0;
            final Short2LongOpenHashMap deleteHeaders = new Short2LongOpenHashMap(amount);
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
                final short valueId = delete.removeShort(0);
                amount--;
                headerOffset = LOOKUP_AMOUNT_SIZE + LOOKUP_ENTRY_SIZE * valueId;
                file.seek(headerOffset);
                file.writeLong(INVALID_HEADER_OFFSET);
                lookupPosition = deleteHeaders.remove(valueId);
                file.seek(lookupPosition + VALUE_HEADER_ID_SIZE);
                dataSize = file.readInt() + VALUE_HEADER_SIZE;
                newFileSize -= dataSize;
                for (int headerIdx = 0; headerIdx < items; headerIdx++) {
                    final long headerValue = headerNewValues.getLong(headerIdx);
                    if (headerValue < lookupPosition) {
                        continue;
                    }
                    headerNewValues.set(headerIdx, headerValue - dataSize);
                }
                for (int entry = 0; entry < amount; entry++) {
                    final short entryId = delete.getShort(entry);
                    final long headerValue = deleteHeaders.get(entryId);
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
            final byte[] buffer = new byte[settings.copyBufferSize];
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
                    final long diff = copyEnd - lookupPosition;
                    final int size = diff > buffer.length ? buffer.length : (int) diff;
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
            access.unlock();
        }
    }

}
