package me.lauriichan.spigot.justlootit.loot.filter.tag;

import me.lauriichan.spigot.justlootit.nms.nbt.TagType;

public interface ITagFilter<T> {
    
    boolean isApplicable(TagType<?> tagType);

    boolean matches(TagType<?> tagType, T value);
    
    @SuppressWarnings("unchecked")
    default boolean matchesAny(TagType<?> tagType, Object value) {
        return matches(tagType, (T) value);
    }

}
