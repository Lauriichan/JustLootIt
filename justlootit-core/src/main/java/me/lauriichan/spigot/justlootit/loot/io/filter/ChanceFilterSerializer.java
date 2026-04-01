package me.lauriichan.spigot.justlootit.loot.io.filter;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.filter.ChanceFilter;

@Extension
@HandlerId("loot/filter/chance")
public class ChanceFilterSerializer extends JsonSerializationHandler<ChanceFilter> {

    public ChanceFilterSerializer(BasePlugin<?> plugin) {
        super(plugin, ChanceFilter.class);
    }

    @Override
    public ChanceFilter deserialize(JsonObject buffer) {
        int threshold = Math.max(0, buffer.getAsInt("threshold", 25));
        int bound = Math.max(threshold, buffer.getAsInt("upper_bound", 100));
        return new ChanceFilter(threshold, bound);
    }

    @Override
    protected void serialize(JsonObject buffer, ChanceFilter value) {
        buffer.put("threshold", value.threshold());
        buffer.put("upper_bound", value.bound());
    }

}
