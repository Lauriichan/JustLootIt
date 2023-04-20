package me.lauriichan.spigot.justlootit.storage.randomaccessfile;

import java.io.Closeable;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import me.lauriichan.spigot.justlootit.storage.Storable;

final class RAFAccess<S extends Storable> implements Closeable {

    private static final Predicate<String> IS_HEX = Pattern.compile("[a-fA-F0-9]+").asMatchPredicate();

    public static final FilenameFilter FILE_FILTER = (dir, name) -> name.endsWith(".jli")
        && IS_HEX.test(name.substring(0, name.length() - 4));

    private final int id;
    private final String hexId;

    private final File file;
    private volatile RandomAccessFile access;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    RAFAccess(final int id, final File directory) {
        this.id = id;
        this.hexId = Integer.toHexString(id);
        this.file = new File(directory, hexId + ".jli");
    }

    RAFAccess(final File file) {
        this.id = 0;
        this.hexId = "0";
        this.file = file;
    }

    public int id() {
        return id;
    }

    public String hexId() {
        return hexId;
    }

    public File file() {
        return file;
    }

    public void writeLock() {
        lock.writeLock().lock();
    }

    public void writeUnlock() {
        lock.writeLock().unlock();
    }

    public void readLock() {
        lock.readLock().lock();
    }

    public void readUnlock() {
        lock.readLock().unlock();
    }

    public boolean exists() {
        return file.exists();
    }

    public boolean isOpen() {
        return access != null;
    }

    public RandomAccessFile open() throws IOException {
        if (access != null) {
            return access;
        }
        if (!file.exists()) {
            final File parentFile = file.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                parentFile.mkdirs();
            }
            file.createNewFile();
        }
        return access = new RandomAccessFile(file, "rw");
    }

    @Override
    public void close() throws IOException {
        if (access != null) {
            access.close();
            access = null;
        }
    }

}
