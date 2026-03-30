package me.lauriichan.spigot.justlootit.loot.modify.filter.tag;

import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.spigot.justlootit.nms.nbt.IListTag;
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;

public record ListTagTypeFilter(ObjectList<TagType<?>> allowed) implements ITagFilter<IListTag<?>> {

    public ListTagTypeFilter(ObjectList<TagType<?>> allowed) {
        this.allowed = ObjectLists.unmodifiable(allowed);
    }

    @Override
    public boolean isApplicable(TagType<?> tagType) {
        return tagType == TagType.LIST;
    }

    @Override
    public boolean matches(TagType<?> tagType, IListTag<?> value) {
        return value.isEmpty() || allowed.contains(value.type());
    }

}
