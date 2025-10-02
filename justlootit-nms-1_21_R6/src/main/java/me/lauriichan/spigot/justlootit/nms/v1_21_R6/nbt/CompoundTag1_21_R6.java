package me.lauriichan.spigot.justlootit.nms.v1_21_R6.nbt;

import java.util.Set;
import java.util.UUID;

import me.lauriichan.spigot.justlootit.nms.nbt.ICompoundTag;
import me.lauriichan.spigot.justlootit.nms.nbt.IListTag;
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;
import me.lauriichan.spigot.justlootit.nms.v1_21_R6.util.NmsHelper1_21_R6;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.*;

public final class CompoundTag1_21_R6 implements ICompoundTag {

    private final CompoundTag compoundTag;

    public CompoundTag1_21_R6() {
        this(new CompoundTag());
    }

    public CompoundTag1_21_R6(CompoundTag compoundTag) {
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
    public TagType<?> getListType(String key) {
        Tag tag = compoundTag.get(key);
        if (tag == null || tag.getId() != TagType.LIST.tagId()) {
            return null;
        }
        return TagType.getType(((ListTag) tag).identifyRawElementType());
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
        byte listType = ((ListTag) tag).identifyRawElementType();
        return listType == type.tagId() || listType == 0;
    }

    @Override
    public byte getByte(String key) {
        return compoundTag.getByteOr(key, (byte) 0);
    }

    @Override
    public boolean getBoolean(String key) {
        return compoundTag.getBooleanOr(key, false);
    }

    @Override
    public short getShort(String key) {
        return compoundTag.getShortOr(key, (short) 0);
    }

    @Override
    public int getInt(String key) {
        return compoundTag.getIntOr(key, 0);
    }

    @Override
    public long getLong(String key) {
        return compoundTag.getLongOr(key, 0L);
    }

    @Override
    public float getFloat(String key) {
        return compoundTag.getFloatOr(key, 0f);
    }

    @Override
    public double getDouble(String key) {
        return compoundTag.getDoubleOr(key, 0d);
    }

    @Override
    public byte[] getByteArray(String key) {
        Tag tag = compoundTag.get(key);
        if (tag == null || tag.getId() != TagType.BYTE_ARRAY.tagId()) {
            return null;
        }
        return ((ByteArrayTag) tag).getAsByteArray();
    }

    @Override
    public int[] getIntArray(String key) {
        Tag tag = compoundTag.get(key);
        if (tag == null || tag.getId() != TagType.INT_ARRAY.tagId()) {
            return null;
        }
        return ((IntArrayTag) tag).getAsIntArray();
    }

    @Override
    public long[] getLongArray(String key) {
        Tag tag = compoundTag.get(key);
        if (tag == null || tag.getId() != TagType.LONG_ARRAY.tagId()) {
            return null;
        }
        return ((LongArrayTag) tag).getAsLongArray();
    }

    @Override
    public UUID getUUID(String key) {
        return compoundTag.read(key, UUIDUtil.CODEC).orElse(null);
    }

    @Override
    public ICompoundTag getCompound(String key) {
        Tag tag = compoundTag.get(key);
        if (tag == null || tag.getId() != TagType.COMPOUND.tagId()) {
            return null;
        }
        return new CompoundTag1_21_R6((CompoundTag) tag);
    }

    @Override
    public IListTag<?> getList(String key) {
        Tag tag = compoundTag.get(key);
        if (tag == null || tag.getId() != TagType.LIST.tagId()) {
            return null;
        }
        ListTag listTag = (ListTag) tag;
        return new ListTag1_21_R6<>(listTag, TagType.getType(listTag.identifyRawElementType()));
    }

    @Override
    public <T> IListTag<T> getList(String key, TagType<T> type) {
        Tag tag = compoundTag.get(key);
        if (tag == null || tag.getId() != TagType.LIST.tagId()) {
            return null;
        }
        ListTag listTag = (ListTag) tag;
        byte listType = listTag.identifyRawElementType();
        if (listType == 0 || listType == type.tagId()) {
            return new ListTag1_21_R6<>(listTag, type);
        }
        return null;
    }

    @Override
    public String getString(String key) {
        Tag tag = compoundTag.get(key);
        if (tag == null || tag.getId() != TagType.STRING.tagId()) {
            return null;
        }
        return ((StringTag) tag).value();
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
        compoundTag.store(key, UUIDUtil.CODEC, uuid);
    }

    @Override
    public void set(String key, ICompoundTag compound) {
        compoundTag.put(key, ((CompoundTag1_21_R6) compound).handle());
    }

    @Override
    public void set(String key, IListTag<?> list) {
        compoundTag.put(key, ((ListTag1_21_R6<?>) list).handle());
    }

    @Override
    public void remove(String key) {
        compoundTag.remove(key);
    }

    @Override
    public Set<String> keys() {
        return compoundTag.keySet();
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
        NmsHelper1_21_R6.clearCompound(compoundTag);
    }

    @Override
    public String asString() {
        return compoundTag.toString();
    }

}
