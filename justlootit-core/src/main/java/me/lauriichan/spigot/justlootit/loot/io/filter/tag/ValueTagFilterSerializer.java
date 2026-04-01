package me.lauriichan.spigot.justlootit.loot.io.filter.tag;

import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.filter.tag.ValueTagFilter;
import me.lauriichan.spigot.justlootit.loot.io.LootIO;
import me.lauriichan.spigot.justlootit.nms.nbt.JsonNbtHelper;
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;

@SuppressWarnings({
    "rawtypes",
    "unchecked"
})
@Extension
@HandlerId("loot/filter/tag/value")
public class ValueTagFilterSerializer extends JsonSerializationHandler<ValueTagFilter> {

    public ValueTagFilterSerializer(BasePlugin<?> plugin) {
        super(plugin, ValueTagFilter.class);
    }

    @Override
    public ValueTagFilter deserialize(JsonObject buffer) {
        TagType<?> tagType = LootIO.readTagType(buffer, "tag_type");
        if (tagType == TagType.COMPOUND || tagType == TagType.LIST) {
            throw new IllegalArgumentException("Value tag filter can not be used with COMPOUND or LIST tag types");
        }
        Object value = null;
        Number num;
        JsonArray array;
        switch (tagType.tagId()) {
        case TagType.ID_BYTE:
            num = buffer.getAsNumber("value");
            if (num != null) {
                value = num.byteValue();
            }
            break;
        case TagType.ID_SHORT:
            num = buffer.getAsNumber("value");
            if (num != null) {
                value = num.shortValue();
            }
            break;
        case TagType.ID_INT:
            num = buffer.getAsNumber("value");
            if (num != null) {
                value = num.intValue();
            }
            break;
        case TagType.ID_LONG:
            num = buffer.getAsNumber("value");
            if (num != null) {
                value = num.longValue();
            }
            break;
        case TagType.ID_FLOAT:
            num = buffer.getAsNumber("value");
            if (num != null) {
                value = num.floatValue();
            }
            break;
        case TagType.ID_DOUBLE:
            num = buffer.getAsNumber("value");
            if (num != null) {
                value = num.doubleValue();
            }
            break;
        case TagType.ID_STRING:
            value = buffer.getAsString("value");
            break;
        case TagType.ID_BYTE_ARRAY:
            array = buffer.getAsArray("value");
            if (array != null) {
                value = JsonNbtHelper.asByteArray(array);
            }
            break;
        case TagType.ID_INT_ARRAY:
            array = buffer.getAsArray("value");
            if (array != null) {
                value = JsonNbtHelper.asIntArray(array);
            }
            break;
        case TagType.ID_LONG_ARRAY:
            array = buffer.getAsArray("value");
            if (array != null) {
                value = JsonNbtHelper.asLongArray(array);
            }
            break;
        }
        if (value == null) {
            throw new IllegalArgumentException("Value tag type mismatch");
        }
        return new ValueTagFilter(tagType, value);
    }

    @Override
    protected void serialize(JsonObject buffer, ValueTagFilter value) {
        LootIO.writeTagType(buffer, "tag_type", value.type());
        switch (value.type().tagId()) {
        case TagType.ID_BYTE:
        case TagType.ID_SHORT:
        case TagType.ID_INT:
        case TagType.ID_LONG:
        case TagType.ID_FLOAT:
        case TagType.ID_DOUBLE:
        case TagType.ID_STRING:
            buffer.put("value", value.value());
            break;
        case TagType.ID_BYTE_ARRAY:
            buffer.put("value", JsonNbtHelper.asJsonArray((byte[]) value.value()));
            break;
        case TagType.ID_INT_ARRAY:
            buffer.put("value", JsonNbtHelper.asJsonArray((int[]) value.value()));
            break;
        case TagType.ID_LONG_ARRAY:
            buffer.put("value", JsonNbtHelper.asJsonArray((long[]) value.value()));
            break;
        }
    }

}
