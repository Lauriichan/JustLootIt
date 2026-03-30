package me.lauriichan.spigot.justlootit.loot.modify.filter.tag;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.spigot.justlootit.nms.nbt.ICompoundTag;
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;

public record CompoundTagFilter(ObjectList<ICompoundFilter> filters, MatchType matchType) implements ITagFilter<ICompoundTag> {

    public static interface ICompoundFilter {

        String key();

        boolean filter(ICompoundTag tag);

    }

    public static record ValueFilter(String key, ITagFilter<?> filter) implements ICompoundFilter {

        @Override
        public boolean filter(ICompoundTag tag) {
            TagType<?> type = tag.getType(key);
            if (type == null || !filter.isApplicable(type)) {
                return false;
            }
            Object obj = switch (type.tagId()) {
            case TagType.ID_BYTE:
                yield tag.getByte(key);
            case TagType.ID_SHORT:
                yield tag.getShort(key);
            case TagType.ID_INT:
                yield tag.getInt(key);
            case TagType.ID_LONG:
                yield tag.getLong(key);
            case TagType.ID_FLOAT:
                yield tag.getFloat(key);
            case TagType.ID_DOUBLE:
                yield tag.getDouble(key);
            case TagType.ID_BYTE_ARRAY:
                yield tag.getByteArray(key);
            case TagType.ID_STRING:
                yield tag.getString(key);
            case TagType.ID_LIST:
                yield tag.getList(key);
            case TagType.ID_COMPOUND:
                yield tag.getCompound(key);
            case TagType.ID_INT_ARRAY:
                yield tag.getIntArray(key);
            case TagType.ID_LONG_ARRAY:
                yield tag.getLongArray(key);
            default:
                yield null;
            };
            if (obj == null) {
                return false;
            }
            return filter.matchesAny(type, obj);
        }

    }

    public static record HasFilter(String key, TagType<?> type, boolean numeric, boolean list) implements ICompoundFilter {

        @Override
        public boolean filter(ICompoundTag tag) {
            if (list) {
                if (numeric) {
                    return tag.has(key, TagType.LIST) && tag.getListType(key).numeric();
                }
                if (type == null) {
                    return tag.has(key, TagType.LIST);
                }
                return tag.hasList(key, type);
            }
            if (numeric) {
                return tag.hasNumeric(key);
            }
            if (type == null) {
                return tag.has(key);
            }
            return tag.has(key, type);
        }

    }

    public static record NotFilter(ICompoundFilter filter) implements ICompoundFilter {

        public String key() {
            return filter.key();
        }

        @Override
        public boolean filter(ICompoundTag tag) {
            return !filter.filter(tag);
        }

    }

    @Override
    public boolean isApplicable(TagType<?> tagType) {
        return tagType == TagType.COMPOUND;
    }

    @Override
    public boolean matches(TagType<?> tagType, ICompoundTag value) {
        return switch (matchType) {
        case ALL_CONTAINED -> {
            for (ICompoundFilter filter : filters) {
                if (!filter.filter(value)) {
                    yield false;
                }
            }
            yield true;
        }
        case EXACT -> {
            ObjectArraySet<String> keys = new ObjectArraySet<>();
            for (ICompoundFilter filter : filters) {
                keys.add(filter.key());
                if (!filter.filter(value)) {
                    yield false;
                }
            }
            yield value.keys().stream().allMatch(keys::contains);
        }
        case ONE_CONTAINED -> {
            for (ICompoundFilter filter : filters) {
                if (filter.filter(value)) {
                    yield true;
                }
            }
            yield false;
        }
        case NONE_CONTAINED -> {
            for (ICompoundFilter filter : filters) {
                if (filter.filter(value)) {
                    yield false;
                }
            }
            yield true;
        }
        };
    }

}
