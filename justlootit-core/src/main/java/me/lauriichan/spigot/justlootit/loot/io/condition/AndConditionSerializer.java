package me.lauriichan.spigot.justlootit.loot.io.condition;

import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.loot.ILootCondition;
import me.lauriichan.spigot.justlootit.loot.condition.AndCondition;

@Extension
@HandlerId("loot/condition/and")
public class AndConditionSerializer extends KeyedJsonSerializationHandler<JsonArray, AndCondition> {

    public AndConditionSerializer(BasePlugin<?> plugin) {
        super(plugin, "conditions", ARRAY, AndCondition.class);
    }

    @Override
    public JsonArray toJson(AndCondition value) {
        return JsonIO.serialize(ioManager, value.conditions());
    }

    @Override
    public AndCondition fromJson(JsonArray json) {
        return new AndCondition(JsonIO.deserialize(ioManager, json, ILootCondition.class));
    }

}
