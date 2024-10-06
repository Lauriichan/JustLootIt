package me.lauriichan.spigot.justlootit.storage;

import io.netty.buffer.ByteBuf;

public abstract class StorageAdapter<T> {

    public static final int MAX_TYPE_ID = 65535;

    protected final Class<T> type;
    protected final int typeId;

    public StorageAdapter(final Class<T> type, final int typeId) {
        this.type = type;
        if (typeId > MAX_TYPE_ID || typeId < 0) {
            throw new IllegalArgumentException("Invalid type id <" + typeId + ">, type id has to be between 0 and " + MAX_TYPE_ID);
        }
        this.typeId = typeId & 0xFFFF;
    }

    public Class<T> type() {
        return type;
    }

    public int typeId() {
        return typeId;
    }

    public abstract void serialize(StorageAdapterRegistry registry, T value, ByteBuf buffer);

    public abstract T deserialize(StorageAdapterRegistry registry, ByteBuf buffer);

}
