package me.lauriichan.spigot.justlootit.loot.modify.io.filter;

import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.loot.modify.ILootFilter;
import me.lauriichan.spigot.justlootit.loot.modify.filter.OrFilter;

@Extension
@HandlerId("loot/filter/or")
public class OrFilterSerializer extends KeyedJsonSerializationHandler<JsonArray, OrFilter> {

    public OrFilterSerializer(BasePlugin<?> plugin) {
        super(plugin, "filters", ARRAY, OrFilter.class);
    }

    @Override
    public JsonArray toJson(OrFilter value) {
        return JsonIO.serialize(ioManager, value.filters());
    }

    @Override
    public OrFilter fromJson(JsonArray json) {
        return new OrFilter(JsonIO.deserialize(ioManager, json, ILootFilter.class));
    }

}
