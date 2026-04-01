package me.lauriichan.spigot.justlootit.loot.io;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.laylib.json.JsonString;
import me.lauriichan.minecraft.pluginbase.io.IOManager;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.loot.filter.tag.MatchType;
import me.lauriichan.spigot.justlootit.loot.modifier.InsertionMode;
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;
import me.lauriichan.spigot.justlootit.nms.util.RegistryUtil;
import me.lauriichan.spigot.justlootit.util.WeightedList;

public final class LootIO {

    private LootIO() {
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

    public static InsertionMode readInsertionMode(JsonObject object, String key) {
        String value = object.getAsString(key);
        if (value == null || value.isBlank()) {
            return InsertionMode.APPEND;
        }
        try {
            return InsertionMode.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return InsertionMode.APPEND;
        }
    }

    public static <T extends Keyed> T asRegistry(Registry<T> registry, String value, T fallback) {
        NamespacedKey entryKey;
        try {
            entryKey = NamespacedKey.fromString(value);
        } catch (IllegalArgumentException iae) {
            if (fallback != null) {
                return fallback;
            }
            throw iae;
        }
        T entry = registry.get(entryKey);
        if (entry == null) {
            if (fallback == null) {
                throw new IllegalArgumentException("Unknown registry value '%s'".formatted(entryKey));
            }
            return fallback;
        }
        return entry;
    }

    public static <T extends Keyed> T readRegistry(Registry<T> registry, JsonObject object, String key, T fallback) {
        String value = object.getAsString(key);
        if (value == null || value.isBlank()) {
            if (fallback == null) {
                throw new IllegalArgumentException("Registry value '%s' was not set".formatted(key));
            }
            return fallback;
        }
        return asRegistry(registry, value, fallback);
    }

    public static <T> WeightedList<T> asWeighted(IOManager ioManager, JsonArray array, String elementKey, Class<T> type) {
        WeightedList<T> list = new WeightedList<>();
        for (IJson<?> entry : array) {
            if (!entry.isObject()) {
                continue;
            }
            JsonObject object = entry.asJsonObject();
            double weight = object.getAsDouble("weight", 1d);
            list.add(weight, JsonIO.deserialize(ioManager, object.getAsObject(elementKey), type));
        }
        return list;
    }

    public static <T> WeightedList<T> readWeighted(IOManager ioManager, JsonObject object, String key, String elementKey, Class<T> type) {
        JsonArray array = object.getAsArray(key);
        if (array == null) {
            throw new IllegalArgumentException("Weighted list '%s' is not set".formatted(key));
        }
        return asWeighted(ioManager, array, elementKey, type);
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

    public static void writeInsertionMode(JsonObject object, String key, InsertionMode insertType) {
        if (insertType == null) {
            insertType = InsertionMode.APPEND;
        }
        object.put(key, IJson.of(insertType.name().toLowerCase()));
    }

    public static JsonString fromRegistry(Keyed value) {
        return IJson.of(RegistryUtil.getKey(value).toString());
    }

    public static void writeRegistry(JsonObject object, String key, Keyed value) {
        object.put(key, fromRegistry(value));
    }

    public static <T> JsonArray fromWeighted(IOManager ioManager, String elementKey, WeightedList<T> list) {
        JsonArray array = new JsonArray();
        for (WeightedList.Entry<T> entry : list) {
            JsonObject object = new JsonObject();
            object.put("weight", entry.weight());
            object.put(elementKey, JsonIO.serialize(ioManager, entry.element()));
            array.add(object);
        }
        return array;
    }

    public static <T> void writeWeighted(IOManager ioManager, JsonObject object, String key, String elementKey, WeightedList<T> list) {
        object.put(key, fromWeighted(ioManager, elementKey, list));
    }

}
