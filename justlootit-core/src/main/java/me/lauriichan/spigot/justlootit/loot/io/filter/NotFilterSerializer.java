package me.lauriichan.spigot.justlootit.loot.io.filter;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.loot.ILootFilter;
import me.lauriichan.spigot.justlootit.loot.filter.NotFilter;

@Extension
@HandlerId("loot/filter/not")
public class NotFilterSerializer extends KeyedJsonSerializationHandler<JsonObject, NotFilter> {

    public NotFilterSerializer(BasePlugin<?> plugin) {
        super(plugin, "filter", OBJECT, NotFilter.class);
    }

    @Override
    public JsonObject toJson(NotFilter value) {
        return JsonIO.serialize(ioManager, value.filter());
    }

    @Override
    public NotFilter fromJson(JsonObject json) {
        return new NotFilter(JsonIO.deserialize(ioManager, json, ILootFilter.class));
    }

}
