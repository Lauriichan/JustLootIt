package me.lauriichan.spigot.justlootit.loot.io.modifier;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.loot.ILootFilter;
import me.lauriichan.spigot.justlootit.loot.ILootModifier;
import me.lauriichan.spigot.justlootit.loot.modifier.FilteredModifier;

@Extension
@HandlerId("loot/modifier/filtered")
public class FilteredModifierSerializer extends JsonSerializationHandler<FilteredModifier> {

    public FilteredModifierSerializer(BasePlugin<?> plugin) {
        super(plugin, FilteredModifier.class);
    }

    @Override
    public FilteredModifier deserialize(JsonObject buffer) {
        ILootFilter filter = JsonIO.deserialize(ioManager, buffer.getAsObject("filter"), ILootFilter.class);
        ILootModifier modifier = JsonIO.deserialize(ioManager, buffer.getAsObject("modifier"), ILootModifier.class);
        return new FilteredModifier(filter, modifier);
    }

    @Override
    protected void serialize(JsonObject buffer, FilteredModifier value) {
        buffer.put("filter", JsonIO.serialize(ioManager, value.filter()));
        buffer.put("modifier", JsonIO.serialize(ioManager, value.modifier()));
    }

}
