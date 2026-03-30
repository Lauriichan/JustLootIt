package me.lauriichan.spigot.justlootit.loot.modify.filter.tag;

import me.lauriichan.spigot.justlootit.nms.nbt.TagType;

public record NotTagFilter<T>(ITagFilter<T> filter) implements ITagFilter<T> {

    @Override
    public boolean isApplicable(TagType<?> tagType) {
        return filter.isApplicable(tagType);
    }

    @Override
    public boolean matches(TagType<?> tagType, T value) {
        return !filter.matches(tagType, value);
    }

}
