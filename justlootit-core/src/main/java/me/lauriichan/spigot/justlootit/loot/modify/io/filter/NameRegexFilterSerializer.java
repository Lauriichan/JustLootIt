package me.lauriichan.spigot.justlootit.loot.modify.io.filter;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.modify.filter.NameRegexFilter;

@Extension
@HandlerId("loot/filter/name_regex")
public class NameRegexFilterSerializer extends JsonSerializationHandler<NameRegexFilter> {

    public NameRegexFilterSerializer(BasePlugin<?> plugin) {
        super(plugin, NameRegexFilter.class);
    }

    @Override
    public NameRegexFilter deserialize(JsonObject buffer) {
        String pattern = buffer.getAsString("pattern");
        boolean withColors = buffer.getAsBoolean("with_colors", false);
        return new NameRegexFilter(pattern, withColors);
    }

    @Override
    protected void serialize(JsonObject buffer, NameRegexFilter value) {
        buffer.put("pattern", value.stringPattern());
        buffer.put("with_colors", value.withColors());
    }

}
