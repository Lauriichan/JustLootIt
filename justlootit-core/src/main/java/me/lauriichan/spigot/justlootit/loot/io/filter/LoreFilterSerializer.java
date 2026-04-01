package me.lauriichan.spigot.justlootit.loot.io.filter;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.filter.LoreRegexFilter;

@Extension
@HandlerId("loot/filter/lore_regex")
public class LoreFilterSerializer extends JsonSerializationHandler<LoreRegexFilter> {

    public LoreFilterSerializer(BasePlugin<?> plugin) {
        super(plugin, LoreRegexFilter.class);
    }

    @Override
    public LoreRegexFilter deserialize(JsonObject buffer) {
        String pattern = buffer.getAsString("pattern");
        boolean withColors = buffer.getAsBoolean("with_colors", false);
        return new LoreRegexFilter(pattern, withColors);
    }

    @Override
    protected void serialize(JsonObject buffer, LoreRegexFilter value) {
        buffer.put("pattern", value.stringPattern());
        buffer.put("with_colors", value.withColors());
    }

}
