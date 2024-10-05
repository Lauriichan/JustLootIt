package me.lauriichan.spigot.justlootit.storage.randomaccessfile.legacy;

import static me.lauriichan.spigot.justlootit.storage.randomaccessfile.legacy.RAFSettingsLegacy.*;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantLock;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.lauriichan.spigot.justlootit.storage.StorageException;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.IRAFEntry;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.IRAFFile;

public final class RAFFileLegacy implements IRAFFile, Closeable {
    
    public static final String FILE_EXTENSION = ".jli";

    public static record RAFEntry(long id, int typeId, ByteBuf buffer) implements IRAFEntry {
        @Override
        public int version() {
            return -1;
        }
    }

    private final int id;
    private final String hexId;

    private final File file;
    private volatile RandomAccessFile fileAccess;

    private final ReentrantLock lock = new ReentrantLock();

    private final RAFSettingsLegacy settings;

    public RAFFileLegacy(final RAFSettingsLegacy settings, final int id, final File directory) {
        this.id = id;
        this.hexId = Integer.toHexString(id);
        this.file = new File(directory, hexId + FILE_EXTENSION);
        this.settings = settings;
    }

    public RAFFileLegacy(final RAFSettingsLegacy settings, final File file) {
        this.id = 0;
        this.hexId = "0";
        this.file = file.isDirectory() ? new File(file, "rafstorage" + FILE_EXTENSION) : file;
        this.settings = settings;
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
        if (id >= settings.valueIdAmount || id < 0) {
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
        if (id >= settings.valueIdAmount || id < 0) {
            throw new StorageException("Unsupported value id '" + id + "'!");
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
        if (entry.id() >= settings.valueIdAmount || entry.id() < 0) {
            throw new StorageException("Unsupported value id '" + id + "'!");
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
                fileAccess.writeShort(entry.version());
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
        if (id >= settings.valueIdAmount || id < 0) {
            throw new StorageException("Unsupported value id '" + id + "'!");
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
     * Open
     */

    @Override
    public void open() throws IOException {
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
        } finally {
            lock.unlock();
        }
    }

    /*
     * Closing
     */

    @Override
    public void close() throws IOException {
        lock.lock();
        try {
            internalClose();
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
        final long oldFileEnd = file.length();
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
        final long oldLength = file.length();
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
