package me.lauriichan.spigot.justlootit.loot.modify.filter.tag;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import me.lauriichan.spigot.justlootit.nms.nbt.TagType;

public record RegexTagFilter(String stringPattern, Predicate<String> predicate) implements ITagFilter<String> {

    public RegexTagFilter(String stringPattern) {
        this(stringPattern, Pattern.compile(stringPattern).asMatchPredicate());
    }

    @Override
    public boolean isApplicable(TagType<?> tagType) {
        return tagType == TagType.STRING;
    }

    @Override
    public boolean matches(TagType<?> tagType, String value) {
        return predicate.test(value);
    }

}
