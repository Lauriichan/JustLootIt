package me.lauriichan.spigot.justlootit.loot.io.condition;

import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.loot.ILootCondition;
import me.lauriichan.spigot.justlootit.loot.condition.OrCondition;

@Extension
@HandlerId("loot/condition/or")
public class OrConditionSerializer extends KeyedJsonSerializationHandler<JsonArray, OrCondition> {

    public OrConditionSerializer(BasePlugin<?> plugin) {
        super(plugin, "conditions", ARRAY, OrCondition.class);
    }

    @Override
    public JsonArray toJson(OrCondition value) {
        return JsonIO.serialize(ioManager, value.conditions());
    }

    @Override
    public OrCondition fromJson(JsonArray json) {
        return new OrCondition(JsonIO.deserialize(ioManager, json, ILootCondition.class));
    }

}
