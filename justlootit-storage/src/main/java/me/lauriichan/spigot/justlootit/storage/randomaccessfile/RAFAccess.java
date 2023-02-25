package me.lauriichan.spigot.justlootit.storage.randomaccessfile;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import me.lauriichan.spigot.justlootit.storage.Storable;

public final class RAFAccess<S extends Storable> implements AutoCloseable {

    private static final Predicate<String> IS_HEX = Pattern.compile("[a-fA-F0-9]+").asMatchPredicate();

    public static final FilenameFilter FILE_FILTER = (dir, name) -> name.endsWith(".jli")
        && IS_HEX.test(name.substring(0, name.length() - 4));

    private final int id;

    private final File file;
    private volatile RandomAccessFile access;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public RAFAccess(final int id, final File directory) {
        this.id = id;
        this.file = new File(directory, Long.toHexString(id) + ".jli");
    }

    public int id() {
        return id;
    }

    public File file() {
        return file;
    }

    public void store(short valueId, S storable) {
        lock.writeLock().lock();
        try {

        } finally {
            lock.writeLock().unlock();
        }
    }

    public S read(short valueId) {
        lock.readLock().lock();
        try {
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isOpen() {
        return access != null;
    }

    public RAFAccess<S> open() throws IOException {
        if (access != null) {
            return this;
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        access = new RandomAccessFile(file, "");
        return this;
    }

    @Override
    public void close() throws Exception {
        if (access != null) {
            access.close();
            access = null;
        }
    }

}
