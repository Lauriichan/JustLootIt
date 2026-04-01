package me.lauriichan.spigot.justlootit.loot.io.filter.tag;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.loot.filter.tag.AndTagFilter;
import me.lauriichan.spigot.justlootit.loot.filter.tag.ITagFilter;

@Extension
@HandlerId("loot/filter/tag/and")
public class AndTagFilterSerializer extends KeyedJsonSerializationHandler<JsonArray, AndTagFilter> {

    public AndTagFilterSerializer(BasePlugin<?> plugin) {
        super(plugin, "tag_filters", ARRAY, AndTagFilter.class);
    }

    @Override
    public JsonArray toJson(AndTagFilter value) {
        return JsonIO.serialize(ioManager, value.filters());
    }

    @Override
    public AndTagFilter fromJson(JsonArray json) {
        ObjectArrayList<ITagFilter<?>> filters = new ObjectArrayList<>();
        JsonIO.deserialize(ioManager, filters, json, ITagFilter.class);
        return new AndTagFilter(filters);
    }

}
