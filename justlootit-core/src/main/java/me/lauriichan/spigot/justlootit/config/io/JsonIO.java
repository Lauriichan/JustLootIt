package me.lauriichan.spigot.justlootit.config.io;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.config.handler.JsonConfigHandler;
import me.lauriichan.minecraft.pluginbase.io.IOManager;
import me.lauriichan.minecraft.pluginbase.io.IOManager.Serialized;
import me.lauriichan.minecraft.pluginbase.io.serialization.SerializationException;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;

@SuppressWarnings({
    "unchecked"
})
public final class JsonIO {

    private JsonIO() {
        throw new UnsupportedOperationException();
    }

    public static <T> ObjectList<T> deserialize(IOManager manager, JsonArray values, Class<T> expectedType) {
        ObjectArrayList<T> list = new ObjectArrayList<>();
        deserialize(manager, list, values, expectedType);
        return list;
    }

    public static <T> void deserialize(IOManager manager, ObjectList<T> list, JsonArray values, Class<?> expectedType) {
        for (IJson<?> entry : values) {
            if (!entry.isObject()) {
                continue;
            }
            T value = (T) deserialize(manager, entry.asJsonObject(), expectedType);
            if (value == null) {
                continue;
            }
            list.add(value);
        }
    }

    public static <T> JsonArray serialize(IOManager manager, ObjectList<T> list) {
        JsonArray array = new JsonArray();
        serialize(manager, array, list);
        return array;
    }

    public static <T> void serialize(IOManager manager, JsonArray output, ObjectList<T> list) {
        for (Object obj : list) {
            JsonObject json = serialize(manager, obj);
            if (json == null) {
                continue;
            }
            output.add(json);
        }
    }

    public static Object deserialize(IOManager manager, JsonObject json) {
        try {
            String handlerId = json.getAsString(JsonConfigHandler.KEY_SERIALIZE_TYPE);
            if (handlerId == null) {
                return null;
            }
            return manager.deserialize(JsonSerializationHandler.class, json, handlerId);
        } catch (SerializationException e) {
            throw new IllegalStateException("Failed to deserialize data", e);
        }
    }

    public static <T> T deserialize(IOManager manager, JsonObject json, Class<T> expectedType) {
        try {
            String handlerId = json.getAsString(JsonConfigHandler.KEY_SERIALIZE_TYPE);
            if (handlerId == null) {
                return null;
            }
            return (T) manager.deserialize(JsonSerializationHandler.class, json, expectedType, handlerId);
        } catch (SerializationException e) {
            throw new IllegalStateException("Failed to deserialize data", e);
        }
    }

    public static JsonObject serialize(IOManager manager, Object object) {
        try {
            Serialized<JsonObject> json = manager.serialize(JsonSerializationHandler.class, object);
            if (json == null) {
                return null;
            }
            JsonObject obj = json.value();
            if (obj.has(JsonConfigHandler.KEY_SERIALIZE_TYPE)) {
                throw new SerializationException("Serialization handler wrote to reserved key!");
            }
            obj.put(JsonConfigHandler.KEY_SERIALIZE_TYPE, json.handlerId());
            return obj;
        } catch (SerializationException e) {
            throw new IllegalStateException("Failed to serialize data", e);
        }
    }

}
