package me.lauriichan.spigot.justlootit.loot.io.filter;

import me.lauriichan.laylib.json.JsonString;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.filter.MaterialFilter;
import me.lauriichan.spigot.justlootit.loot.io.LootIO;
import me.lauriichan.spigot.justlootit.loot.io.LootRegistry;

@Extension
@HandlerId("loot/filter/material")
public class MaterialFilterSerializer extends KeyedJsonSerializationHandler<JsonString, MaterialFilter> {

    public MaterialFilterSerializer(BasePlugin<?> plugin) {
        super(plugin, "item_material", STRING, MaterialFilter.class);
    }

    @Override
    public JsonString toJson(MaterialFilter value) {
        return LootIO.fromRegistry(value.material());
    }

    @Override
    public MaterialFilter fromJson(JsonString json) {
        return new MaterialFilter(LootIO.asRegistry(LootRegistry.REGISTRY.material(), json.value(), null));
    }

}
