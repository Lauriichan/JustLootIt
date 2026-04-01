package me.lauriichan.spigot.justlootit.loot.io.filter;

import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.loot.ILootFilter;
import me.lauriichan.spigot.justlootit.loot.filter.AndFilter;

@Extension
@HandlerId("loot/filter/and")
public class AndFilterSerializer extends KeyedJsonSerializationHandler<JsonArray, AndFilter> {

    public AndFilterSerializer(BasePlugin<?> plugin) {
        super(plugin, "filters", ARRAY, AndFilter.class);
    }

    @Override
    public JsonArray toJson(AndFilter value) {
        return JsonIO.serialize(ioManager, value.filters());
    }

    @Override
    public AndFilter fromJson(JsonArray json) {
        return new AndFilter(JsonIO.deserialize(ioManager, json, ILootFilter.class));
    }

}
