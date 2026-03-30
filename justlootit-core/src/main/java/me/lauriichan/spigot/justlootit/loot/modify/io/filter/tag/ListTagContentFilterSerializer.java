package me.lauriichan.spigot.justlootit.loot.modify.io.filter.tag;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.loot.modify.filter.tag.ITagFilter;
import me.lauriichan.spigot.justlootit.loot.modify.filter.tag.ListTagContentFilter;
import me.lauriichan.spigot.justlootit.loot.modify.filter.tag.MatchType;
import me.lauriichan.spigot.justlootit.loot.modify.io.FilterIO;
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;

@SuppressWarnings({
    "rawtypes",
    "unchecked"
})
@Extension
@HandlerId("loot/filter/tag/list/content")
public class ListTagContentFilterSerializer extends JsonSerializationHandler<ListTagContentFilter> {

    public ListTagContentFilterSerializer(BasePlugin<?> plugin) {
        super(plugin, ListTagContentFilter.class);
    }

    @Override
    public ListTagContentFilter deserialize(JsonObject buffer) {
        TagType<?> type = FilterIO.readTagType(buffer, "component_tag_type");
        if (type == null) {
            throw new IllegalArgumentException("'component_tag_type' has to be set");
        }
        MatchType matchType = FilterIO.readMatchType(buffer, "match_type");
        ObjectArrayList<ITagFilter<?>> filters = new ObjectArrayList<>();
        JsonIO.deserialize(ioManager, filters, buffer.getAsArray("tag_filters"), ITagFilter.class);
        return new ListTagContentFilter<>(type, matchType, filters);
    }

    @Override
    protected void serialize(JsonObject buffer, ListTagContentFilter value) {
        FilterIO.writeTagType(buffer, "component_tag_type", value.componentType());
        FilterIO.writeMatchType(buffer, "match_type", value.matchType());
        JsonArray array = new JsonArray();
        buffer.put("tag_filters", array);
        JsonIO.serialize(ioManager, array, value.filters());
    }

}
