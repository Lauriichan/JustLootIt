package me.lauriichan.spigot.justlootit.loot.modify.filter.tag;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.spigot.justlootit.nms.nbt.IListTag;
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;

public record ListTagContentFilter<T>(TagType<T> componentType, MatchType matchType, ObjectList<ITagFilter<?>> filters)
    implements ITagFilter<IListTag<?>> {

    public ListTagContentFilter(TagType<T> componentType, MatchType matchType, ObjectList<ITagFilter<?>> filters) {
        this.componentType = componentType;
        this.matchType = matchType == null ? MatchType.ONE_CONTAINED : matchType;
        for (ITagFilter<?> filter : filters) {
            if (!filter.isApplicable(componentType)) {
                throw new IllegalArgumentException("Unsupported filter for component type '%s'".formatted(componentType.name()));
            }
        }
        this.filters = ObjectLists.unmodifiable(filters);
    }

    @Override
    public boolean isApplicable(TagType<?> tagType) {
        return tagType == TagType.LIST;
    }

    @Override
    public boolean matches(TagType<?> tagType, IListTag<?> value) {
        if (value.type() == componentType) {
            return false;
        }
        return switch (matchType) {
        case ALL_CONTAINED -> {
            ObjectArrayList<Object> list = new ObjectArrayList<>();
            value.forEach(list::add);
            for (ITagFilter<?> filter : filters) {
                if (list.isEmpty()) {
                    yield false;
                }
                for (Object obj : list) {
                    if (filter.matchesAny(tagType, obj)) {
                        list.remove(obj);
                        break;
                    }
                }
            }
            yield true;
        }
        case EXACT -> {
            if (filters.size() != value.size()) {
                yield false;
            }
            for (int i = 0; i < filters.size(); i++) {
                if (!filters.get(i).matchesAny(tagType, value.get(i))) {
                    yield false;
                }
            }
            yield true;
        }
        case ONE_CONTAINED -> {
            for (Object obj : value) {
                for (ITagFilter<?> filter : filters) {
                    if (filter.matchesAny(tagType, obj)) {
                        yield true;
                    }
                }
            }
            yield false;
        }
        case NONE_CONTAINED -> {
            for (Object obj : value) {
                for (ITagFilter<?> filter : filters) {
                    if (filter.matchesAny(tagType, obj)) {
                        yield false;
                    }
                }
            }
            yield true;
        }
        };
    }

}
