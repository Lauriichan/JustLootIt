package me.lauriichan.spigot.justlootit.storage.identifier;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongLists;
import me.lauriichan.laylib.logger.ISimpleLogger;

public final class FileIdentifier implements IIdentifier {

    private final ISimpleLogger logger;

    private final File file;
    private final ReentrantLock lock = new ReentrantLock();

    private final LongList list = LongLists.synchronize(new LongArrayList());

    private final AtomicLong nextId = new AtomicLong(0L);
    private long lastSaved = 0L;

    public FileIdentifier(ISimpleLogger logger, File file) {
        this.logger = logger;
        this.file = createFile(file);
        this.lastSaved = file.lastModified();
    }

    private File createFile(File file) {
        if (file.isDirectory()) {
            return new File(file, "storage.id");
        }
        if (file.getName().endsWith(".id") || file.getParent() == null) {
            return file;
        }
        return new File(file.getParentFile(), file.getName() + ".id");
    }

    @Override
    public long nextId() {
        lock.lock();
        try {
            if (!list.isEmpty()) {
                return list.removeLong(0);
            }
            return nextId.getAndIncrement();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void reset() {
        lock.lock();
        try {
            nextId.set(0);
            list.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void delete(long id) {
        lock.lock();
        try {
            long current = nextId.get();
            if (id > current) {
                return;
            } else if (id == current) {
                nextId.decrementAndGet();
                int idx;
                while ((idx = list.indexOf(nextId.get())) != -1) {
                    list.removeLong(idx);
                    nextId.decrementAndGet();
                }
                return;
            }
            list.add(id);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public long lastSaved() {
        return lastSaved;
    }

    @Override
    public void load() {
        lock.lock();
        try {
            list.clear();
            if (!file.exists()) {
                nextId.set(0);
                ;
                return;
            }
            try (DataInputStream data = new DataInputStream(new FileInputStream(file))) {
                nextId.set(data.readLong());
                int size;
                try {
                    size = data.readInt();
                } catch (EOFException eof) {
                    return;
                }
                for (int i = 0; i < size; i++) {
                    list.add(data.readLong());
                }
            }
        } catch (IOException e) {
            logger.warning("Unable to load identifier data from file '" + file.getAbsolutePath() + "'!", e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void save() {
        lock.lock();
        try {
            if (!file.exists()) {
                if (file.getParent() != null && !file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }
            try (DataOutputStream data = new DataOutputStream(new FileOutputStream(file))) {
                data.writeLong(nextId.get());
                data.writeInt(list.size());
                if (!list.isEmpty()) {
                    for (long value : list) {
                        data.writeLong(value);
                    }
                }
            }
        } catch (IOException e) {
            logger.warning("Unable to save identifier data to file '" + file.getAbsolutePath() + "'!", e);
        } finally {
            lastSaved = System.currentTimeMillis();
            lock.unlock();
        }
    }

}
