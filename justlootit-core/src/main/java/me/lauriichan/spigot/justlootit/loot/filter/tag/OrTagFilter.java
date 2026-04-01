package me.lauriichan.spigot.justlootit.loot.filter.tag;

import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;

public record OrTagFilter(ObjectList<ITagFilter<?>> filters) implements ITagFilter<Object> {

    @Override
    public boolean isApplicable(TagType<?> tagType) {
        return filters.stream().anyMatch(filter -> filter.isApplicable(tagType));
    }

    @Override
    public boolean matches(TagType<?> tagType, Object value) {
        for (ITagFilter<?> filter : filters) {
            if (filter.isApplicable(tagType) && filter.matchesAny(tagType, value)) {
                return true;
            }
        }
        return false;
    }

}
