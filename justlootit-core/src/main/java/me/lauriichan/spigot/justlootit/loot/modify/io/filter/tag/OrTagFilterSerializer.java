package me.lauriichan.spigot.justlootit.loot.modify.io.filter.tag;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.loot.modify.filter.tag.ITagFilter;
import me.lauriichan.spigot.justlootit.loot.modify.filter.tag.OrTagFilter;

@Extension
@HandlerId("loot/filter/tag/or")
public class OrTagFilterSerializer extends KeyedJsonSerializationHandler<JsonArray, OrTagFilter> {

    public OrTagFilterSerializer(BasePlugin<?> plugin) {
        super(plugin, "tag_filters", ARRAY, OrTagFilter.class);
    }

    @Override
    public JsonArray toJson(OrTagFilter value) {
        return JsonIO.serialize(ioManager, value.filters());
    }

    @Override
    public OrTagFilter fromJson(JsonArray json) {
        ObjectArrayList<ITagFilter<?>> filters = new ObjectArrayList<>();
        JsonIO.deserialize(ioManager, filters, json, ITagFilter.class);
        return new OrTagFilter(filters);
    }

}
