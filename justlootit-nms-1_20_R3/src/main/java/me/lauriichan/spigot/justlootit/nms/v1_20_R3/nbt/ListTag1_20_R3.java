package me.lauriichan.spigot.justlootit.nms.v1_20_R3.nbt;

import java.util.Iterator;

import me.lauriichan.spigot.justlootit.nms.nbt.IListTag;
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;
import net.minecraft.nbt.ListTag;

public final class ListTag1_20_R3<T> implements IListTag<T> {

    private static final class SimpleIterator<T> implements Iterator<T> {

        private final ListTag1_20_R3<T> tag;
        private volatile int index;

        public SimpleIterator(final ListTag1_20_R3<T> tag) {
            this.tag = tag;
        }

        @Override
        public boolean hasNext() {
            return index < tag.size();
        }

        @Override
        public T next() {
            return tag.get(index);
        }

    }

    private final ListTag listTag;
    private final NbtType1_20_R3<T, ?> type;

    public ListTag1_20_R3(TagType<T> type) {
        this(new ListTag(), type);
    }

    public ListTag1_20_R3(ListTag listTag, TagType<T> type) {
        this.listTag = listTag;
        this.type = NbtType1_20_R3.get(type);
    }

    public ListTag handle() {
        return listTag;
    }

    @Override
    public int size() {
        return listTag.size();
    }

    @Override
    public TagType<T> type() {
        return type.type();
    }

    @Override
    public Iterator<T> iterator() {
        return new SimpleIterator<>(this);
    }

    @Override
    public void add(T value) {
        listTag.add(type.fromPrimitive(value));
    }

    @Override
    public void add(int index, T value) {
        listTag.add(index, type.fromPrimitive(value));
    }

    @Override
    public void clear() {
        listTag.clear();
    }

    @Override
    public boolean contains(T value) {
        return listTag.contains(type.fromPrimitive(value));
    }

    @Override
    public T get(int index) {
        return type.tryToPrimitive(listTag.get(index));
    }

    @Override
    public boolean isEmpty() {
        return listTag.isEmpty();
    }

    @Override
    public T remove(int index) {
        return type.tryToPrimitive(listTag.remove(index));
    }

    @Override
    public void set(int index, T value) {
        listTag.set(index, type.fromPrimitive(value));
    }

}
