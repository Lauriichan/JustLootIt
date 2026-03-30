package me.lauriichan.spigot.justlootit.loot.modify.io.filter.tag;

import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.modify.filter.tag.ListTagTypeFilter;
import me.lauriichan.spigot.justlootit.loot.modify.io.FilterIO;

@Extension
@HandlerId("loot/filter/tag/list/type")
public class ListTagTypeFilterSerializer extends KeyedJsonSerializationHandler<JsonArray, ListTagTypeFilter> {

    public ListTagTypeFilterSerializer(BasePlugin<?> plugin) {
        super(plugin, "allowed_component_types", ARRAY, ListTagTypeFilter.class);
    }

    @Override
    public JsonArray toJson(ListTagTypeFilter value) {
        return FilterIO.fromTagTypes(value.allowed());
    }

    @Override
    public ListTagTypeFilter fromJson(JsonArray json) {
        return new ListTagTypeFilter(FilterIO.asTagTypes(json));
    }

}
