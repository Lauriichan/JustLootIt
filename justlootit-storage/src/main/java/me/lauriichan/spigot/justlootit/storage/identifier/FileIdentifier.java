package me.lauriichan.spigot.justlootit.storage.identifier;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import me.lauriichan.laylib.logger.ISimpleLogger;

public final class FileIdentifier implements IIdentifier {

    private final ISimpleLogger logger;

    private final File file;
    private final ReentrantLock lock = new ReentrantLock();

    private long nextId = 0L;
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
            return nextId++;
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void reset() {
        lock.lock();
        try {
            nextId = 0L;
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void set(long id) {
        lock.lock();
        try {
            nextId = Math.max(id, 0L);
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
            if (!file.exists()) {
                nextId = 0L;
                return;
            }
            try (DataInputStream data = new DataInputStream(new FileInputStream(file))) {
                nextId = data.readLong();
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
                data.writeLong(nextId);
            }
        } catch (IOException e) {
            logger.warning("Unable to save identifier data to file '" + file.getAbsolutePath() + "'!", e);
        } finally {
            lastSaved = System.currentTimeMillis();
            lock.unlock();
        }
    }

}
