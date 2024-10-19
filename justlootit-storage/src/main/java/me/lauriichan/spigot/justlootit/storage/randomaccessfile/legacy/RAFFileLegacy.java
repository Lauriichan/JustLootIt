package me.lauriichan.spigot.justlootit.storage.randomaccessfile.legacy;

import static me.lauriichan.spigot.justlootit.storage.randomaccessfile.legacy.RAFSettingsLegacy.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.shorts.Short2LongOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import me.lauriichan.spigot.justlootit.storage.StorageException;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.IRAFEntry;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.IRAFFile;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.IRAFSettings;

public final class RAFFileLegacy implements IRAFFile {

    public static record RAFEntry(long id, int typeId, ByteBuf buffer) implements IRAFEntry {
        @Override
        public int version() {
            return -1;
        }
    }
    
    public static final String FILE_EXTENSION = ".jli";
    
    public static File create(File file) {
        if (file.isDirectory()) {
            return new File(file, "rafstorage" + FILE_EXTENSION);
        }
        String name = file.getName();
        if (!name.endsWith(FILE_EXTENSION)) {
            int idx = name.lastIndexOf('.');
            if (idx == -1) {
                return new File(file.getParent(), file.getName() + FILE_EXTENSION);
            }
            return new File(file.getParent(), name.substring(0, idx) + FILE_EXTENSION);
        }
        return file;
    }
    
    public static File create(File directory, int id) {
        return new File(directory, Integer.toHexString(id) + FILE_EXTENSION);
    }

    private final int id;
    private final long idBase;
    
    private final String hexId;

    private final File file;
    private volatile RandomAccessFile fileAccess;

    private final ReentrantLock lock = new ReentrantLock();

    private final RAFSettingsLegacy settings;

    public RAFFileLegacy(final RAFSettingsLegacy settings, final File directory, final int id) {
        this.id = id;
        this.hexId = Integer.toHexString(id);
        this.file = create(directory, id);
        this.settings = settings;
        this.idBase = id == -1 ? 0 : id << settings.valueIdBits;
    }

    public RAFFileLegacy(final RAFSettingsLegacy settings, final File file) {
        this.id = -1;
        this.hexId = "";
        this.file = create(file);
        this.settings = settings;
        this.idBase = id == -1 ? 0 : id << settings.valueIdBits;
    }
    
    private boolean isInvalidId(long id) {
        if (this.id != -1) {
            return false;
        }
        return id >= settings.valueIdAmount || id < 0;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public String hexId() {
        return hexId;
    }

    @Override
    public int version() {
        return -1;
    }
    
    @Override
    public IRAFSettings settings() {
        return settings;
    }

    @Override
    public File file() {
        return file;
    }

    @Override
    public boolean isOpen() {
        return fileAccess != null;
    }
    
    /*
     * Has
     */

    @Override
    public boolean has(long id) {
        if (!isOpen()) {
            throw new StorageException("File is not open");
        }
        if (isInvalidId(id)) {
            return false;
        }
        final int valueId = (int) (id & settings.valueIdMask);
        lock.lock();
        try {
            final long fileSize = fileAccess.length();
            if (fileSize == 0) {
                internalCloseDelete();
                return false;
            }
            fileAccess.seek(LOOKUP_ENTRY_BASE_OFFSET + LOOKUP_ENTRY_SIZE * valueId);
            return fileAccess.readLong() != INVALID_HEADER_OFFSET;
        } catch (final IOException e) {
            throw new StorageException("Failed to check if value with id '" + Long.toHexString(id) + "' exists!", e);
        } finally {
            lock.unlock();
        }
    }
    
    /*
     * Read
     */

    @Override
    public RAFEntry read(final long id) {
        if (!isOpen()) {
            throw new StorageException("File is not open");
        }
        if (isInvalidId(id)) {
            throw new StorageException("Invalid value id '" + id + "'!");
        }
        final int valueId = (int) (id & settings.valueIdMask);
        lock.lock();
        try {
            final long fileSize = fileAccess.length();
            if (fileSize == 0) {
                internalCloseDelete();
                return null;
            }
            fileAccess.seek(LOOKUP_ENTRY_BASE_OFFSET + LOOKUP_ENTRY_SIZE * valueId);
            final long lookupPosition = fileAccess.readLong();
            if (lookupPosition == INVALID_HEADER_OFFSET) {
                return null;
            }
            fileAccess.seek(lookupPosition);
            final int typeId = fileAccess.readUnsignedShort();
            final byte[] buffer = new byte[fileAccess.readInt()];
            fileAccess.read(buffer);
            return new RAFEntry(id, typeId, Unpooled.wrappedBuffer(buffer));
        } catch (final IOException e) {
            throw new StorageException("Failed to read value with id '" + Long.toHexString(id) + "' from file!", e);
        } finally {
            lock.unlock();
        }
    }
    
    /*
     * Write
     */

    @Override
    public void write(IRAFEntry entry) {
        if (!isOpen()) {
            throw new StorageException("File is not open");
        }
        if (isInvalidId(entry.id())) {
            throw new StorageException("Invalid value id '" + id + "'!");
        }
        entry.buffer().resetReaderIndex();
        final int bufferSize = entry.buffer().readableBytes();
        final int valueId = (int) (entry.id() & settings.valueIdMask);
        lock.lock();
        try {
            final long fileSize = fileAccess.length();
            if (fileSize == 0) {
                fileAccess.setLength(settings.lookupHeaderSize + bufferSize);
                fileAccess.seek(FORMAT_VERSION);
                fileAccess.writeShort(1);
                fileAccess.skipBytes(LOOKUP_ENTRY_SIZE * valueId);
                fileAccess.writeLong(settings.lookupHeaderSize);
                fileAccess.seek(settings.lookupHeaderSize);
                fileAccess.writeShort(entry.typeId());
                fileAccess.writeInt(bufferSize);
                entry.buffer().readBytes(fileAccess.getChannel(), fileAccess.getFilePointer(), bufferSize);
                return;
            }
            final long headerOffset = LOOKUP_ENTRY_BASE_OFFSET + LOOKUP_ENTRY_SIZE * valueId;
            fileAccess.seek(headerOffset);
            long lookupPosition = fileAccess.readLong();
            if (lookupPosition != INVALID_HEADER_OFFSET) {
                fileAccess.seek(lookupPosition + VALUE_HEADER_ID_VERSION_SIZE);
                final int dataSize = fileAccess.readInt();
                final long offset = updateFileSize(lookupPosition, dataSize, bufferSize);
                if (offset != 0) {
                    fileAccess.seek(LOOKUP_ENTRY_BASE_OFFSET);
                    final long oldDataEnd = lookupPosition + dataSize + VALUE_HEADER_SIZE;
                    while (fileAccess.getFilePointer() != settings.lookupHeaderSize) {
                        final long entryOffset = fileAccess.readLong();
                        if (entryOffset < oldDataEnd) {
                            continue;
                        }
                        fileAccess.seek(fileAccess.getFilePointer() - LOOKUP_ENTRY_SIZE);
                        fileAccess.writeLong(entryOffset + offset);
                    }
                }
                fileAccess.seek(lookupPosition);
                fileAccess.writeShort(entry.typeId());
                fileAccess.writeInt(bufferSize);
                entry.buffer().readBytes(fileAccess.getChannel(), fileAccess.getFilePointer(), bufferSize);
                return;
            }
            fileAccess.seek(FORMAT_VERSION);
            final int amount = fileAccess.readUnsignedShort();
            fileAccess.seek(FORMAT_VERSION);
            fileAccess.writeShort(amount + 1);
            fileAccess.setLength(fileSize + bufferSize + VALUE_HEADER_SIZE);
            fileAccess.seek(headerOffset);
            fileAccess.writeLong(lookupPosition = fileSize);
            fileAccess.seek(lookupPosition);
            fileAccess.writeShort(entry.typeId());
            fileAccess.writeInt(bufferSize);
            entry.buffer().readBytes(fileAccess.getChannel(), fileAccess.getFilePointer(), bufferSize);
        } catch (final IOException e) {
            throw new StorageException("Failed to write value with id '" + Long.toHexString(entry.id()) + "' to file!", e);
        } finally {
            lock.unlock();
        }
    }
    
    /*
     * Delete
     */

    @Override
    public boolean delete(long id) {
        if (!isOpen()) {
            throw new StorageException("File is not open");
        }
        if (isInvalidId(id)) {
            throw new StorageException("Invalid value id '" + id + "'!");
        }
        final int valueId = (int) (id & settings.valueIdMask);
        lock.lock();
        try {
            final long fileSize = fileAccess.length();
            if (fileSize == 0) {
                internalCloseDelete();
                return false;
            }
            final long headerOffset = LOOKUP_ENTRY_BASE_OFFSET + LOOKUP_ENTRY_SIZE * valueId;
            fileAccess.seek(headerOffset);
            final long lookupPosition = fileAccess.readLong();
            if (lookupPosition == INVALID_HEADER_OFFSET) {
                return false;
            }
            fileAccess.seek(lookupPosition + VALUE_HEADER_ID_VERSION_SIZE);
            final int dataSize = fileAccess.readInt();
            if (deleteEntry(lookupPosition, dataSize, headerOffset)) {
                internalCloseDelete();
            }
            return true;
        } catch (final IOException e) {
            throw new StorageException("Failed to delete value with id '" + Long.toHexString(id) + "' from file!", e);
        } finally {
            lock.unlock();
        }
    }

    private boolean deleteEntry(final long lookupPosition, final int dataSize, final long headerOffset)
        throws IOException {
        fileAccess.seek(FORMAT_VERSION);
        final int amount = fileAccess.readUnsignedShort();
        if (amount - 1 == 0) {
            return true;
        }
        fileAccess.seek(FORMAT_VERSION);
        fileAccess.writeShort(amount - 1);
        fileAccess.seek(headerOffset);
        fileAccess.writeLong(INVALID_HEADER_OFFSET);
        final long offset = updateFileSize(lookupPosition, dataSize + VALUE_HEADER_SIZE, 0);
        if (offset != 0) {
            fileAccess.seek(LOOKUP_ENTRY_BASE_OFFSET);
            while (fileAccess.getFilePointer() != settings.lookupHeaderSize) {
                final long entryOffset = fileAccess.readLong();
                if (entryOffset < lookupPosition) {
                    continue;
                }
                fileAccess.seek(fileAccess.getFilePointer() - LOOKUP_ENTRY_SIZE);
                fileAccess.writeLong(entryOffset + offset);
            }
        }
        return false;
    }
    
    /*
     * For each
     */
    
    @Override
    public void forEach(Consumer<IRAFEntry> consumer) throws StorageException {
        if (!isOpen()) {
            throw new StorageException("File is not open");
        }
        lock.lock();
        try {
            final long fileSize = fileAccess.length();
            if (fileSize == 0) {
                internalCloseDelete();
                return;
            }
            long headerOffset;
            long lookupPosition;
            for (int valueId = 0; valueId < settings.valueIdAmount; valueId++) {
                headerOffset = LOOKUP_ENTRY_BASE_OFFSET + LOOKUP_ENTRY_SIZE * valueId;
                fileAccess.seek(headerOffset);
                lookupPosition = fileAccess.readLong();
                if (lookupPosition == INVALID_HEADER_OFFSET) {
                    continue;
                }
                fileAccess.seek(lookupPosition);
                final long id = idBase + valueId;
                final int typeId = fileAccess.readUnsignedShort();
                final byte[] buffer = new byte[fileAccess.readInt()];
                fileAccess.read(buffer);
                consumer.accept(new RAFEntry(id, typeId, Unpooled.wrappedBuffer(buffer)));
            }
        } catch (final IOException e) {
            throw new StorageException("Failed to read through all entries from file!", e);
        } finally {
            lock.unlock();
        }
    }
    
    /*
     * Modify each
     */
    
    @Override
    public void modifyEach(Function<IRAFEntry, IRAFEntry> func) throws StorageException {
        if (!isOpen()) {
            throw new StorageException("File is not open");
        }
        if (!isOpen()) {
            throw new StorageException("File is not open");
        }
        lock.lock();
        try {
            long fileSize = fileAccess.length();
            if (fileSize == 0) {
                internalCloseDelete();
                return;
            }
            ShortArrayList deleteList = new ShortArrayList();
            fileAccess.seek(FORMAT_VERSION);
            int items = fileAccess.readShort();
            long headerOffset;
            long lookupPosition;
            for (int valueId = 0; valueId < settings.valueIdAmount; valueId++) {
                headerOffset = LOOKUP_ENTRY_BASE_OFFSET + LOOKUP_ENTRY_SIZE * valueId;
                fileAccess.seek(headerOffset);
                lookupPosition = fileAccess.readLong();
                if (lookupPosition == INVALID_HEADER_OFFSET) {
                    continue;
                }
                final long id = idBase + valueId;
                fileAccess.seek(lookupPosition);
                final int typeId = fileAccess.readUnsignedShort();
                final int dataSize = fileAccess.readInt();
                final byte[] buffer = new byte[dataSize];
                fileAccess.read(buffer);
                IRAFEntry entry = new RAFEntry(id, typeId, Unpooled.wrappedBuffer(buffer));
                IRAFEntry result = func.apply(entry);
                if (result == entry) {
                    continue;
                }
                if (result == null) {
                    if ((items -= 1) == 0) {
                        internalCloseDelete();
                        return; // File is gone
                    }
                    deleteList.add((short) valueId);
                    continue;
                }
                result.buffer().resetReaderIndex();
                final int bufferSize = result.buffer().readableBytes();
                final long offset = updateFileSize(lookupPosition, dataSize, bufferSize);
                if (offset != 0) {
                    fileAccess.seek(LOOKUP_ENTRY_BASE_OFFSET);
                    final long oldDataEnd = lookupPosition + dataSize + VALUE_HEADER_SIZE;
                    while (fileAccess.getFilePointer() != settings.lookupHeaderSize) {
                        final long entryOffset = fileAccess.readLong();
                        if (entryOffset < oldDataEnd) {
                            continue;
                        }
                        fileAccess.seek(fileAccess.getFilePointer() - LOOKUP_ENTRY_SIZE);
                        fileAccess.writeLong(entryOffset + offset);
                    }
                }
                fileAccess.seek(lookupPosition);
                fileAccess.writeShort(result.typeId());
                fileAccess.writeInt(bufferSize);
                result.buffer().readBytes(fileAccess.getChannel(), fileAccess.getFilePointer(), bufferSize);
            }
            if (deleteList.isEmpty()) {
                return;
            }
            int amount = deleteList.size();
            fileAccess.seek(FORMAT_VERSION);
            fileAccess.writeShort(items);
            // Here we delete all entries mentioned above
            // This should speed up this process by a lot compared to individual delete operations
            fileSize = fileAccess.length();
            final Long2IntOpenHashMap keysToIndex = new Long2IntOpenHashMap(items);
            final LongArrayList headerKeys = new LongArrayList(items);
            final LongArrayList headerValues = new LongArrayList(items);
            final LongArrayList headerNewValues = new LongArrayList(items);
            int headerAmount = 0;
            final Short2LongOpenHashMap deleteHeaders = new Short2LongOpenHashMap(amount);
            final Short2LongOpenHashMap modifiedDeleteHeaders = new Short2LongOpenHashMap(amount);
            for (int valueId = 0; valueId < settings.valueIdAmount; valueId++) {
                headerOffset = LOOKUP_ENTRY_BASE_OFFSET + LOOKUP_ENTRY_SIZE * valueId;
                fileAccess.seek(headerOffset);
                lookupPosition = fileAccess.readLong();
                if (lookupPosition == INVALID_HEADER_OFFSET) {
                    continue;
                }
                if (deleteList.contains((short) valueId)) {
                    deleteHeaders.put((short) valueId, lookupPosition);
                    modifiedDeleteHeaders.put((short) valueId, lookupPosition);
                    continue;
                }
                keysToIndex.put(headerOffset, headerAmount++);
                headerKeys.add(headerOffset);
                headerValues.add(lookupPosition);
                headerNewValues.add(lookupPosition);
            }
            int dataSize;
            long offsetLookupPosition;
            while (amount != 0) {
                final short valueIdShort = deleteList.removeShort(0);
                final int valueId = Short.toUnsignedInt(valueIdShort);
                amount--;
                offsetLookupPosition = modifiedDeleteHeaders.remove(valueIdShort);
                headerOffset = LOOKUP_ENTRY_BASE_OFFSET + LOOKUP_ENTRY_SIZE * valueId;
                fileAccess.seek(headerOffset);
                fileAccess.writeLong(INVALID_HEADER_OFFSET);
                lookupPosition = deleteHeaders.remove(valueIdShort);
                fileAccess.seek(lookupPosition + VALUE_HEADER_ID_VERSION_SIZE);
                dataSize = fileAccess.readInt() + VALUE_HEADER_SIZE;
                fileSize -= dataSize;
                for (int headerIdx = 0; headerIdx < items; headerIdx++) {
                    final long headerValue = headerNewValues.getLong(headerIdx);
                    if (headerValue < offsetLookupPosition) {
                        continue;
                    }
                    headerNewValues.set(headerIdx, headerValue - dataSize);
                }
                for (int entry = 0; entry < amount; entry++) {
                    final short entryId = deleteList.getShort(entry);
                    final long headerValue = modifiedDeleteHeaders.get(entryId);
                    if (headerValue < offsetLookupPosition) {
                        continue;
                    }
                    modifiedDeleteHeaders.put(entryId, headerValue - dataSize);
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
                fileAccess.seek(headerOffset);
                fileAccess.writeLong(copyTo);
                fileAccess.seek(copyFrom + VALUE_HEADER_ID_VERSION_SIZE);
                dataSize = fileAccess.readInt() + VALUE_HEADER_SIZE;
                fileAccess.seek(copyFrom);
                lookupPosition = copyFrom;
                copyEnd = copyFrom + dataSize;
                copyAmount = 0;
                while (lookupPosition != copyEnd) {
                    final long diff = copyEnd - lookupPosition;
                    final int size = diff > buffer.length ? buffer.length : (int) diff;
                    fileAccess.seek(lookupPosition);
                    fileAccess.read(buffer, 0, size);
                    fileAccess.seek(copyTo + copyAmount);
                    fileAccess.write(buffer, 0, size);
                    lookupPosition += size;
                    copyAmount += size;
                }
            }
            fileAccess.setLength(fileSize);
        } catch (final IOException e) {
            throw new StorageException("Failed to modify all entries from file!", e);
        } finally {
            lock.unlock();
        }
    }
    
    /*
     * Open
     */

    @Override
    public void open() throws StorageException {
        lock.lock();
        try {
            if (fileAccess != null) {
                return;
            }
            if (!file.exists()) {
                final File parentFile = file.getParentFile();
                if (parentFile != null && !parentFile.exists()) {
                    parentFile.mkdirs();
                }
                file.createNewFile();
            }
            fileAccess = new RandomAccessFile(file, "rw");
        } catch (IOException exp) {
            throw new StorageException("Failed to open file '" + file.getName() + "'", exp);
        } finally {
            lock.unlock();
        }
    }

    /*
     * Closing
     */

    @Override
    public void close() throws StorageException {
        lock.lock();
        try {
            internalClose();
        } catch (IOException exp) {
            throw new StorageException("Failed to close file '" + file.getName() + "'", exp);
        } finally {
            lock.unlock();
        }
    }

    private void internalClose() throws IOException {
        if (fileAccess == null) {
            return;
        }
        fileAccess.close();
        fileAccess = null;
    }

    private void internalCloseDelete() throws IOException {
        internalClose();
        file.delete();
    }

    /*
     * File size management
     */

    private long updateFileSize(final long offset, final long oldSize, final long newSize) throws IOException {
        final long difference = newSize - oldSize;
        if (difference == 0) {
            return 0;
        }
        if (difference < 0) {
            shrinkFile(offset + newSize, difference * -1);
            return difference;
        }
        expandFile(offset + oldSize, difference);
        return difference;
    }

    private void expandFile(final long offset, final long amount) throws IOException {
        final long oldFileEnd = fileAccess.length();
        fileAccess.setLength(oldFileEnd + amount);
        long pointer = oldFileEnd;
        final byte[] buffer = new byte[settings.copyBufferSize];
        while (pointer != offset) {
            final long diff = pointer - offset;
            final int size = diff > buffer.length ? buffer.length : (int) diff;
            pointer -= size;
            fileAccess.seek(pointer);
            fileAccess.read(buffer, 0, size);
            fileAccess.seek(pointer + amount);
            fileAccess.write(buffer, 0, size);
        }
    }

    private void shrinkFile(final long offset, final long amount) throws IOException {
        long pointer = offset + amount;
        final long oldLength = fileAccess.length();
        final long newLength = oldLength - amount;
        final byte[] buffer = new byte[settings.copyBufferSize];
        while (pointer != oldLength) {
            final long diff = oldLength - pointer;
            final int size = diff > buffer.length ? buffer.length : (int) diff;
            fileAccess.seek(pointer);
            fileAccess.read(buffer, 0, size);
            fileAccess.seek(pointer - amount);
            fileAccess.write(buffer, 0, size);
            pointer += size;
        }
        fileAccess.setLength(newLength);
    }

}
