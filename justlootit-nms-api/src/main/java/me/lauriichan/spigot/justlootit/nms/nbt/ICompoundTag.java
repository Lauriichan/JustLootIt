package me.lauriichan.spigot.justlootit.nms.nbt;

import java.util.UUID;

public interface ICompoundTag {
    
    TagType<?> getType(String key);
    
    boolean isType(String key, TagType<?> type);
    
    boolean isListType(String key, TagType<?> type);
    
    boolean isNumeric(String key);
    
    byte getByte(String key);
    
    boolean getBoolean(String key);
    
    short getShort(String key);
    
    int getInt(String key);
    
    long getLong(String key);
    
    float getFloat(String key);
    
    double getDouble(String key);
    
    String getString(String key);
    
    byte[] getByteArray(String key);
    
    int[] getIntArray(String key);
    
    long[] getLongArray(String key);
    
    UUID getUUID(String key);
    
    ICompoundTag getCompound(String key);
    
    <T> IListTag<T> getList(String key, TagType<T> type);
    
    void set(String key, byte value);
    
    void set(String key, boolean value);
    
    void set(String key, short value);
    
    void set(String key, int value);
    
    void set(String key, long value);
    
    void set(String key, float value);
    
    void set(String key, double value);
    
    void set(String key, String value);
    
    void set(String key, byte[] value);
    
    void set(String key, int[] value);
    
    void set(String key, long[] value);
    
    void set(String key, UUID uuid);
    
    void set(String key, ICompoundTag compound);
    
    void set(String key, IListTag<?> list);
    
    int size();
    
    boolean isEmpty();
    
    void clear();
    
}