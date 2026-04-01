package me.lauriichan.spigot.justlootit.loot.filter.tag;

import me.lauriichan.spigot.justlootit.nms.nbt.TagType;

public record RangeTagFilter(double min, double max) implements ITagFilter<Number> {

    @Override
    public boolean isApplicable(TagType<?> tagType) {
        return tagType.numeric();
    }

    @Override
    public boolean matches(TagType<?> tagType, Number value) {
        var doubleValue = value.doubleValue();
        return doubleValue >= min && doubleValue <= max;
    }

}
