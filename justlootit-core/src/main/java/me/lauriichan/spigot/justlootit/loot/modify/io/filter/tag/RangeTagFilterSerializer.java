package me.lauriichan.spigot.justlootit.loot.modify.io.filter.tag;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.modify.filter.tag.RangeTagFilter;

@Extension
@HandlerId("loot/filter/tag/range")
public class RangeTagFilterSerializer extends JsonSerializationHandler<RangeTagFilter> {

    public RangeTagFilterSerializer(BasePlugin<?> plugin) {
        super(plugin, RangeTagFilter.class);
    }

    @Override
    public RangeTagFilter deserialize(JsonObject buffer) {
        double min = buffer.getAsDouble("min", 0d);
        double max = buffer.getAsDouble("max", 0d);
        return new RangeTagFilter(Math.min(min, max), Math.max(min, max));
    }

    @Override
    protected void serialize(JsonObject buffer, RangeTagFilter value) {
        buffer.put("min", value.min());
        buffer.put("max", value.max());
    }

}
