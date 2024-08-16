package me.lauriichan.spigot.justlootit.nms.nbt;

import java.util.Map;

import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.IJsonNumber;
import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.laylib.json.JsonType;

final class JsonNbtHelper {

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final long[] EMPTY_LONG_ARRAY = new long[0];

    private JsonNbtHelper() {
        throw new UnsupportedOperationException();
    }

    /*
     * From json to nbt
     */

    public static ICompoundTag asCompoundTag(NbtHelper nbt, JsonObject json) {
        ICompoundTag tag = nbt.createCompound();
        for (Map.Entry<String, IJson<?>> entry : json.entrySet()) {
            setToTag(nbt, entry.getKey(), entry.getValue(), tag);
        }
        return tag;
    }

    private static void setToTag(NbtHelper nbt, String key, IJson<?> value, ICompoundTag tag) {
        if (value.isPrimitive()) {
            if (value.isBoolean()) {
                tag.set(key, value.asBoolean());
                return;
            }
            if (value.isString()) {
                tag.set(key, value.asString());
                return;
            }
            if (value.isNumber()) {
                IJsonNumber<?> number = value.asJsonNumber();
                switch (number.type()) {
                case BYTE:
                    tag.set(key, number.asByte());
                    return;
                case DOUBLE:
                    tag.set(key, number.asDouble());
                    return;
                case FLOAT:
                    tag.set(key, number.asFloat());
                    return;
                case INTEGER:
                    tag.set(key, number.asInt());
                    return;
                case LONG:
                    tag.set(key, number.asLong());
                    return;
                case SHORT:
                    tag.set(key, number.asShort());
                    return;
                default:
                    return;
                }
            }
            return;
        }
        if (!value.isObject()) {
            return;
        }
        JsonObject valueObj = value.asJsonObject();
        if (!valueObj.has("type", JsonType.STRING)) {
            return;
        }
        TagType<?> type = TagType.getType(valueObj.getAsString("type"));
        if (valueObj.has("value")) {
            IJson<?> json = valueObj.get("value");
            if (!json.isNumber() || !type.numeric()) {
                setToTag(nbt, key, json, tag);
                return;
            }
            Number number = json.asNumber();
            if (type == TagType.BYTE) {
                tag.set(key, number.byteValue());
                return;
            }
            if (type == TagType.SHORT) {
                tag.set(key, number.shortValue());
                return;
            }
            if (type == TagType.INT) {
                tag.set(key, number.intValue());
                return;
            }
            if (type == TagType.LONG) {
                tag.set(key, number.longValue());
                return;
            }
            if (type == TagType.FLOAT) {
                tag.set(key, number.floatValue());
                return;
            }
            if (type == TagType.DOUBLE) {
                tag.set(key, number.doubleValue());
                return;
            }
        } else if (valueObj.has("list", JsonType.ARRAY)) {
            JsonArray array = valueObj.getAsArray("list");
            if (valueObj.getAsBoolean("array", false)) {
                if (type == TagType.BYTE) {
                    tag.set(key, asByteArray(array));
                    return;
                } else if (type == TagType.INT) {
                    tag.set(key, asIntArray(array));
                    return;
                } else if (type == TagType.LONG) {
                    tag.set(key, asLongArray(array));
                    return;
                }
            }
            tag.set(key, asListTag(nbt, type, array));
            return;
        }
    }

    public static IListTag<?> asListTag(NbtHelper nbt, JsonObject json) {
        TagType<?> type = TagType.getType(json.getAsString("type"));
        return asListTag(nbt, type, json.getAsArray("list"));
    }

    public static <T> IListTag<T> asListTag(NbtHelper nbt, TagType<T> type, JsonArray json) {
        IListTag<T> tag = nbt.createList(type);
        for (IJson<?> value : json) {
            if (value == null) {
                continue;
            }
            if (type == TagType.BYTE_ARRAY) {
                if (!value.isArray()) {
                    continue;
                }
                tag.add(type.tagType().cast(asByteArray(value.asJsonArray())));
                continue;
            }
            if (type == TagType.INT_ARRAY) {
                if (!value.isArray()) {
                    continue;
                }
                tag.add(type.tagType().cast(asIntArray(value.asJsonArray())));
                continue;
            }
            if (type == TagType.LONG_ARRAY) {
                if (!value.isArray()) {
                    continue;
                }
                tag.add(type.tagType().cast(asLongArray(value.asJsonArray())));
                continue;
            }
            if (type == TagType.COMPOUND) {
                if (!value.isObject()) {
                    continue;
                }
                tag.add(type.tagType().cast(asCompoundTag(nbt, value.asJsonObject())));
                continue;
            }
            if (type == TagType.STRING) {
                if (!value.isString()) {
                    continue;
                }
                tag.add(type.tagType().cast(value.asString()));
                continue;
            }
            if (!value.isNumber()) {
                continue;
            }
            Number numValue = value.asNumber();
            switch (type.tagId()) {
            case TagType.ID_BYTE:
                tag.add(type.tagType().cast(numValue.byteValue()));
                break;
            case TagType.ID_SHORT:
                tag.add(type.tagType().cast(numValue.shortValue()));
                break;
            case TagType.ID_INT:
                tag.add(type.tagType().cast(numValue.intValue()));
                break;
            case TagType.ID_LONG:
                tag.add(type.tagType().cast(numValue.longValue()));
                break;
            case TagType.ID_FLOAT:
                tag.add(type.tagType().cast(numValue.floatValue()));
                break;
            case TagType.ID_DOUBLE:
                tag.add(type.tagType().cast(numValue.doubleValue()));
                break;
            }
        }
        return tag;
    }

    private static byte[] asByteArray(JsonArray array) {
        if (array.isEmpty()) {
            return EMPTY_BYTE_ARRAY;
        }
        byte[] output = new byte[array.size()];
        int index = 0;
        for (IJson<?> value : array) {
            if (value == null || !value.isNumber()) {
                continue;
            }
            output[index++] = value.asNumber().byteValue();
        }
        if (index != output.length) {
            if (index == 0) {
                return EMPTY_BYTE_ARRAY;
            }
            byte[] tmp = new byte[index];
            System.arraycopy(output, 0, tmp, 0, index);
            return tmp;
        }
        return output;
    }

    private static int[] asIntArray(JsonArray array) {
        if (array.isEmpty()) {
            return EMPTY_INT_ARRAY;
        }
        int[] output = new int[array.size()];
        int index = 0;
        for (IJson<?> value : array) {
            if (value == null || !value.isNumber()) {
                continue;
            }
            output[index++] = value.asNumber().intValue();
        }
        if (index != output.length) {
            if (index == 0) {
                return EMPTY_INT_ARRAY;
            }
            int[] tmp = new int[index];
            System.arraycopy(output, 0, tmp, 0, index);
            return tmp;
        }
        return output;
    }

    private static long[] asLongArray(JsonArray array) {
        if (array.isEmpty()) {
            return EMPTY_LONG_ARRAY;
        }
        long[] output = new long[array.size()];
        int index = 0;
        for (IJson<?> value : array) {
            if (value == null || !value.isNumber()) {
                continue;
            }
            output[index++] = value.asNumber().longValue();
        }
        if (index != output.length) {
            if (index == 0) {
                return EMPTY_LONG_ARRAY;
            }
            long[] tmp = new long[index];
            System.arraycopy(output, 0, tmp, 0, index);
            return tmp;
        }
        return output;
    }

    /*
     * From nbt to json
     */

    public static JsonObject asJson(ICompoundTag tag) {
        JsonObject object = new JsonObject();
        if (tag.isEmpty()) {
            return object;
        }
        for (String key : tag.keys()) {
            TagType<?> type = tag.getType(key);
            switch (type.tagId()) {
            case TagType.ID_BYTE:
                object.put(key, asJson(tag.getByte(key)));
                break;
            case TagType.ID_SHORT:
                object.put(key, asJson(tag.getShort(key)));
                break;
            case TagType.ID_INT:
                object.put(key, asJson(tag.getInt(key)));
                break;
            case TagType.ID_LONG:
                object.put(key, asJson(tag.getLong(key)));
                break;
            case TagType.ID_FLOAT:
                object.put(key, asJson(tag.getFloat(key)));
                break;
            case TagType.ID_DOUBLE:
                object.put(key, asJson(tag.getDouble(key)));
                break;
            case TagType.ID_STRING:
                object.put(key, IJson.of(tag.getString(key)));
                break;
            case TagType.ID_LIST:
                IListTag<?> list = tag.getList(key);
                if (list.type() == null) {
                    break;
                }
                object.put(key, asJson(list));
                break;
            case TagType.ID_COMPOUND:
                object.put(key, asJson(tag.getCompound(key)));
                break;
            case TagType.ID_BYTE_ARRAY:
                object.put(key, asJson(tag.getByteArray(key)));
                break;
            case TagType.ID_INT_ARRAY:
                object.put(key, asJson(tag.getIntArray(key)));
                break;
            case TagType.ID_LONG_ARRAY:
                object.put(key, asJson(tag.getLongArray(key)));
                break;
            }
        }
        return object;
    }

    public static JsonObject asJson(IListTag<?> tag) {
        if (tag.type() == null) {
            throw new IllegalArgumentException("Unsupported list tag, no type available");
        }
        JsonObject object = new JsonObject();
        object.put("type", tag.type().name());
        JsonArray output = new JsonArray();
        if (tag.type() == TagType.COMPOUND) {
            for (Object rawValue : tag) {
                output.add(asJson((ICompoundTag) rawValue));
            }
        } else if (tag.type() == TagType.BYTE_ARRAY) {
            for (Object rawValue : tag) {
                output.add(asJson((byte[]) rawValue));
            }
        } else if (tag.type() == TagType.INT_ARRAY) {
            for (Object rawValue : tag) {
                output.add(asJson((int[]) rawValue));
            }
        } else if (tag.type() == TagType.LONG_ARRAY) {
            for (Object rawValue : tag) {
                output.add(asJson((long[]) rawValue));
            }
        } else if (tag.type() == TagType.LIST) {
            for (Object rawValue : tag) {
                IListTag<?> listTag = (IListTag<?>) rawValue;
                if (listTag.type() == null) {
                    continue;
                }
                output.add(asJson(listTag));
            }
        } else {
            for (Object value : tag) {
                output.add(IJson.of(value));
            }
        }
        object.put("list", output);
        return object;
    }

    private static JsonObject asJson(byte value) {
        JsonObject object = new JsonObject();
        object.put("type", TagType.BYTE.name());
        object.put("value", IJson.of(value));
        return object;
    }

    private static JsonObject asJson(short value) {
        JsonObject object = new JsonObject();
        object.put("type", TagType.SHORT.name());
        object.put("value", IJson.of(value));
        return object;
    }

    private static JsonObject asJson(int value) {
        JsonObject object = new JsonObject();
        object.put("type", TagType.INT.name());
        object.put("value", IJson.of(value));
        return object;
    }

    private static JsonObject asJson(long value) {
        JsonObject object = new JsonObject();
        object.put("type", TagType.LONG.name());
        object.put("value", IJson.of(value));
        return object;
    }

    private static JsonObject asJson(float value) {
        JsonObject object = new JsonObject();
        object.put("type", TagType.FLOAT.name());
        object.put("value", IJson.of(value));
        return object;
    }

    private static JsonObject asJson(double value) {
        JsonObject object = new JsonObject();
        object.put("type", TagType.DOUBLE.name());
        object.put("value", IJson.of(value));
        return object;
    }

    private static JsonObject asJson(byte[] array) {
        JsonObject object = new JsonObject();
        object.put("type", TagType.BYTE.name());
        object.put("array", true);
        JsonArray output = new JsonArray();
        for (byte value : array) {
            output.add(IJson.of(value));
        }
        object.put("list", output);
        return object;
    }

    private static JsonObject asJson(int[] array) {
        JsonObject object = new JsonObject();
        object.put("type", TagType.INT.name());
        object.put("array", true);
        JsonArray output = new JsonArray();
        for (int value : array) {
            output.add(IJson.of(value));
        }
        object.put("list", output);
        return object;
    }

    private static JsonObject asJson(long[] array) {
        JsonObject object = new JsonObject();
        object.put("type", TagType.LONG.name());
        object.put("array", true);
        JsonArray output = new JsonArray();
        for (long value : array) {
            output.add(IJson.of(value));
        }
        object.put("list", output);
        return object;
    }

}
