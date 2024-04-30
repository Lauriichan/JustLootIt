package me.lauriichan.spigot.justlootit.nms.v1_20_R4.nbt;

import java.util.UUID;

import me.lauriichan.spigot.justlootit.nms.nbt.ICompoundTag;
import me.lauriichan.spigot.justlootit.nms.nbt.IListTag;
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;
import me.lauriichan.spigot.justlootit.nms.v1_20_R4.util.NmsHelper1_20_R4;
import net.minecraft.nbt.*;

public final class CompoundTag1_20_R4 implements ICompoundTag {

    private final CompoundTag compoundTag;

    public CompoundTag1_20_R4() {
        this(new CompoundTag());
    }

    public CompoundTag1_20_R4(CompoundTag compoundTag) {
        this.compoundTag = compoundTag;
    }

    public CompoundTag handle() {
        return compoundTag;
    }

    @Override
    public TagType<?> getType(String key) {
        Tag tag = compoundTag.get(key);
        if (tag == null) {
            return null;
        }
        return TagType.getType(tag.getId());
    }
    
    @Override
    public boolean has(String key) {
        return compoundTag.contains(key);
    }

    @Override
    public boolean has(String key, TagType<?> type) {
        return type == getType(key);
    }
    
    @Override
    public boolean hasNumeric(String key) {
        TagType<?> type = getType(key);
        return type != null && type.numeric();
    }

    @Override
    public boolean hasList(String key, TagType<?> type) {
        Tag tag = compoundTag.get(key);
        if (tag == null || tag.getId() != TagType.LIST.tagId()) {
            return false;
        }
        byte listType = ((ListTag) tag).getElementType();
        return listType == type.tagId() || listType == 0;
    }

    @Override
    public byte getByte(String key) {
        return compoundTag.getByte(key);
    }
    
    @Override
    public boolean getBoolean(String key) {
        return compoundTag.getBoolean(key);
    }

    @Override
    public short getShort(String key) {
        return compoundTag.getShort(key);
    }

    @Override
    public int getInt(String key) {
        return compoundTag.getInt(key);
    }

    @Override
    public long getLong(String key) {
        return compoundTag.getLong(key);
    }

    @Override
    public float getFloat(String key) {
        return compoundTag.getFloat(key);
    }

    @Override
    public double getDouble(String key) {
        return compoundTag.getDouble(key);
    }

    @Override
    public byte[] getByteArray(String key) {
        if (compoundTag.contains(key, TagType.BYTE_ARRAY.tagId())) {
            return ((ByteArrayTag) compoundTag.get(key)).getAsByteArray();
        }
        return null;
    }

    @Override
    public int[] getIntArray(String key) {
        if (compoundTag.contains(key, TagType.INT_ARRAY.tagId())) {
            return ((IntArrayTag) compoundTag.get(key)).getAsIntArray();
        }
        return null;
    }

    @Override
    public long[] getLongArray(String key) {
        if (compoundTag.contains(key, TagType.LONG_ARRAY.tagId())) {
            return ((LongArrayTag) compoundTag.get(key)).getAsLongArray();
        }
        return null;
    }
    
    @Override
    public UUID getUUID(String key) {
        try {
            return compoundTag.getUUID(key);
        } catch(IllegalArgumentException ignore) {
            return null;
        }
    }

    @Override
    public ICompoundTag getCompound(String key) {
        if (compoundTag.contains(key, TagType.COMPOUND.tagId())) {
            return new CompoundTag1_20_R4((CompoundTag) compoundTag.get(key));
        }
        return null;
    }

    @Override
    public <T> IListTag<T> getList(String key, TagType<T> type) {
        if (compoundTag.contains(key, TagType.LIST.tagId())) {
            ListTag listTag = (ListTag) compoundTag.get(key);
            byte listType = listTag.getElementType();
            if (listType == type.tagId() || listType == 0) {
                return new ListTag1_20_R4<>(listTag, type);
            }
        }
        return null;
    }

    @Override
    public String getString(String key) {
        if (compoundTag.contains(key, TagType.STRING.tagId())) {
            return ((StringTag) compoundTag.get(key)).getAsString();
        }
        return null;
    }

    @Override
    public void set(String key, byte value) {
        compoundTag.putByte(key, value);
    }
    
    @Override
    public void set(String key, boolean value) {
        compoundTag.putBoolean(key, value);
    }

    @Override
    public void set(String key, short value) {
        compoundTag.putShort(key, value);
    }

    @Override
    public void set(String key, int value) {
        compoundTag.putInt(key, value);
    }

    @Override
    public void set(String key, long value) {
        compoundTag.putLong(key, value);
    }

    @Override
    public void set(String key, float value) {
        compoundTag.putFloat(key, value);
    }

    @Override
    public void set(String key, double value) {
        compoundTag.putDouble(key, value);
    }

    @Override
    public void set(String key, String value) {
        compoundTag.putString(key, value);
    }

    @Override
    public void set(String key, byte[] value) {
        compoundTag.putByteArray(key, value);
    }

    @Override
    public void set(String key, int[] value) {
        compoundTag.putIntArray(key, value);
    }

    @Override
    public void set(String key, long[] value) {
        compoundTag.putLongArray(key, value);
    }
    
    @Override
    public void set(String key, UUID uuid) {
        compoundTag.putUUID(key, uuid);
    }

    @Override
    public void set(String key, ICompoundTag compound) {
        compoundTag.put(key, ((CompoundTag1_20_R4) compound).handle());
    }

    @Override
    public void set(String key, IListTag<?> list) {
        compoundTag.put(key, ((ListTag1_20_R4<?>) list).handle());
    }
    
    @Override
    public void remove(String key) {
        compoundTag.remove(key);
    }
    
    @Override
    public int size() {
        return compoundTag.size();
    }
    
    @Override
    public boolean isEmpty() {
        return compoundTag.isEmpty();
    }
    
    @Override
    public void clear() {
        NmsHelper1_20_R4.clearCompound(compoundTag);
    }
    
    @Override
    public String asString() {
        return compoundTag.getAsString();
    }

}
