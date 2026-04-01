package me.lauriichan.spigot.justlootit.loot.filter.tag;

import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;

public record AndTagFilter(ObjectList<ITagFilter<?>> filters) implements ITagFilter<Object> {

    @Override
    public boolean isApplicable(TagType<?> tagType) {
        return filters.stream().allMatch(filter -> filter.isApplicable(tagType));
    }

    @Override
    public boolean matches(TagType<?> tagType, Object value) {
        for (ITagFilter<?> filter : filters) {
            if (!filter.matchesAny(tagType, value)) {
                return false;
            }
        }
        return true;
    }

}
