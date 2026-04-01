package me.lauriichan.spigot.justlootit.loot.io.filter.tag;

import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.JsonString;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.filter.tag.RegexTagFilter;

@Extension
@HandlerId("loot/filter/tag/regex")
public class RegexTagFilterSerializer extends KeyedJsonSerializationHandler<JsonString, RegexTagFilter> {

    public RegexTagFilterSerializer(BasePlugin<?> plugin) {
        super(plugin, "pattern", STRING, RegexTagFilter.class);
    }

    @Override
    public JsonString toJson(RegexTagFilter value) {
        return IJson.of(value.stringPattern());
    }

    @Override
    public RegexTagFilter fromJson(JsonString json) {
        return new RegexTagFilter(json.asString());
    }

}
