package me.lauriichan.spigot.justlootit.loot.io.condition;

import java.util.regex.Pattern;

import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.JsonString;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.condition.LootTableRegexCondition;

@Extension
@HandlerId("loot/condition/loottable_regex")
public class LootTableRegexConditionSerializer extends KeyedJsonSerializationHandler<JsonString, LootTableRegexCondition> {

    public LootTableRegexConditionSerializer(BasePlugin<?> plugin) {
        super(plugin, "pattern", STRING, LootTableRegexCondition.class);
    }

    @Override
    public JsonString toJson(LootTableRegexCondition value) {
        return IJson.of(value.stringPattern());
    }

    @Override
    public LootTableRegexCondition fromJson(JsonString json) {
        return new LootTableRegexCondition(json.value(), Pattern.compile(json.value()).asMatchPredicate());
    }

}
