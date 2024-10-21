package me.lauriichan.spigot.justlootit.storage;

import java.util.Map;
import java.util.Objects;

import io.netty.buffer.ByteBuf;
import me.lauriichan.laylib.logger.ISimpleLogger;

public final class Stored<T> {
    
    private final StorageAdapterRegistry registry;

    private final StorageAdapter<T> adapter;
    
    private volatile long id = -1;
    private volatile int version;
    private volatile T value;

    private volatile boolean dirty;

    public Stored(final StorageAdapterRegistry registry, final StorageAdapter<T> adapter, final int version) {
        this.registry = registry;
        this.adapter = adapter;
        this.version = version;
    }

    public Stored(final StorageAdapterRegistry registry, final StorageAdapter<T> adapter) {
        this.registry = registry;
        this.adapter = adapter;
        this.version = registry.migrator().getTargetVersion(adapter.type());
    }
    
    public StorageAdapter<T> adapter() {
        return adapter;
    }

    public int version() {
        return version;
    }

    public T value() {
        return value;
    }
    
    public <E> E valueAs(Class<E> type) {
        return type.cast(value);
    }

    public Stored<T> value(Object rawValue) {
        this.value = (T) rawValue;
        return this;
    }
    
    public boolean isEmpty() {
        return value == null;
    }
    
    public boolean isPresent() {
        return value != null;
    }
    
    public <E> Stored<E> cast() {
        return (Stored<E>) this;
    }
    
    public long id() {
        return id;
    }
    
    public boolean needsId() {
        return id == -1;
    }
    
    public Stored<T> id(long id) {
        this.id = id;
        return this;
    }
    
    public void read(ISimpleLogger logger, ByteBuf buffer) {
        if (registry.migrator().needsMigration(adapter.type(), version)) {
            Map.Entry<Integer, ByteBuf> entry = registry.migrator().migrate(id, adapter.type(), version, buffer);
            version = entry.getKey().intValue();
            buffer = entry.getValue();
        }
        value = adapter.deserialize(registry, buffer);
    }
    
    public void write(ISimpleLogger logger, ByteBuf buffer) {
        adapter.serialize(registry, value, buffer);
    }
    
    public void setDirty() {
        this.dirty = true;
    }
    
    public void unsetDirty() {
        this.dirty = false;
        if (value instanceof IModifiable modifiable) {
            modifiable.unsetDirty();
        }
    }

    public boolean isDirty() {
        return dirty || (value instanceof IModifiable modifiable && modifiable.isDirty());
    }

}
