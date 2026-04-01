package me.lauriichan.spigot.justlootit.loot.io.filter.tag;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.loot.filter.tag.ITagFilter;
import me.lauriichan.spigot.justlootit.loot.filter.tag.NotTagFilter;

@SuppressWarnings({
    "rawtypes",
    "unchecked"
})
@Extension
@HandlerId("loot/filter/tag/not")
public class NotTagFilterSerializer extends KeyedJsonSerializationHandler<JsonObject, NotTagFilter> {

    public NotTagFilterSerializer(BasePlugin<?> plugin) {
        super(plugin, "tag_filter", OBJECT, NotTagFilter.class);
    }

    @Override
    public JsonObject toJson(NotTagFilter value) {
        return JsonIO.serialize(ioManager, value.filter());
    }

    @Override
    public NotTagFilter fromJson(JsonObject json) {
        return new NotTagFilter<>(JsonIO.deserialize(ioManager, json, ITagFilter.class));
    }

}
