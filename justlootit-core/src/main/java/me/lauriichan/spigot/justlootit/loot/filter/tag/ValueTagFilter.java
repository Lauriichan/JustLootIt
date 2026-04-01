package me.lauriichan.spigot.justlootit.loot.filter.tag;

import me.lauriichan.spigot.justlootit.nms.nbt.TagType;

public final record ValueTagFilter<T>(TagType<T> type, T value) implements ITagFilter<T> {
    
    public ValueTagFilter(TagType<T> type, T value) {
        if (type == TagType.COMPOUND || type == TagType.LIST) {
            throw new IllegalArgumentException("Value tag filter can not be used with COMPOUND or LIST tag types");
        }
        this.type = type;
        this.value = value;
    }

    @Override
    public boolean isApplicable(TagType<?> tagType) {
        return type == tagType;
    }

    @Override
    public boolean matches(TagType<?> tagType, T value) {
        return this.value.equals(value);
    }

}
