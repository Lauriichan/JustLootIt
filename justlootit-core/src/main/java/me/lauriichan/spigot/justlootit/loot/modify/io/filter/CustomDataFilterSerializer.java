package me.lauriichan.spigot.justlootit.loot.modify.io.filter;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.IOManager.Serialized;
import me.lauriichan.minecraft.pluginbase.io.serialization.SerializationException;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.modify.filter.CustomDataFilter;
import me.lauriichan.spigot.justlootit.loot.modify.filter.tag.CompoundTagFilter;

@SuppressWarnings("unchecked")
@Extension
@HandlerId("loot/filter/custom_data")
public class CustomDataFilterSerializer extends JsonSerializationHandler<CustomDataFilter> {

    public CustomDataFilterSerializer(BasePlugin<?> plugin) {
        super(plugin, CustomDataFilter.class);
    }

    @Override
    public CustomDataFilter deserialize(JsonObject buffer) {
        try {
            return new CustomDataFilter(ioManager.deserialize(JsonSerializationHandler.class, buffer, CompoundTagFilter.class));
        } catch (SerializationException e) {
            throw new IllegalStateException("Couldn't deserialize custom data filter", e);
        }
    }

    @Override
    protected void serialize(JsonObject buffer, CustomDataFilter value) {
        try {
            buffer.putAll(((Serialized<JsonObject>) ioManager.serialize(JsonSerializationHandler.class, value)).value());
        } catch (SerializationException e) {
            throw new IllegalStateException("Couldn't serialize custom data filter", e);
        }
    }

}
