package me.lauriichan.spigot.justlootit.loot.modify.io;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.spigot.justlootit.loot.modify.filter.tag.MatchType;
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;

public final class FilterIO {

    private FilterIO() {
        throw new UnsupportedOperationException();
    }

    public static ObjectList<TagType<?>> asTagTypes(JsonArray array) {
        ObjectArrayList<TagType<?>> list = new ObjectArrayList<>();
        for (IJson<?> json : array) {
            if (!json.isString()) {
                continue;
            }
            TagType<?> type = TagType.getType(json.asString().toUpperCase());
            if (type == null || list.contains(type)) {
                continue;
            }
            list.add(type);
        }
        return list;
    }

    public static TagType<?> readTagType(JsonObject object, String key) {
        String value = object.getAsString(key);
        if (value == null || value.isBlank()) {
            return null;
        }
        return TagType.getType(value.toUpperCase());
    }

    public static MatchType readMatchType(JsonObject object, String key) {
        String value = object.getAsString(key);
        if (value == null || value.isBlank()) {
            return MatchType.ONE_CONTAINED;
        }
        try {
            return MatchType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return MatchType.ONE_CONTAINED;
        }
    }

    public static JsonArray fromTagTypes(ObjectList<TagType<?>> types) {
        JsonArray array = new JsonArray();
        for (TagType<?> type : types) {
            array.add(IJson.of(type.name().toLowerCase()));
        }
        return array;
    }

    public static void writeTagType(JsonObject object, String key, TagType<?> type) {
        if (type == null) {
            object.put(key, IJson.NULL);
            return;
        }
        object.put(key, IJson.of(type.name().toLowerCase()));
    }

    public static void writeMatchType(JsonObject object, String key, MatchType type) {
        if (type == null) {
            type = MatchType.ONE_CONTAINED;
        }
        object.put(key, IJson.of(type.name().toLowerCase()));
    }

}
