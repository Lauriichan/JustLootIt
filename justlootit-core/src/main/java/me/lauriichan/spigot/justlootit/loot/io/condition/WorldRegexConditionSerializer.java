package me.lauriichan.spigot.justlootit.loot.io.condition;

import java.util.regex.Pattern;

import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.JsonString;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.condition.WorldRegexCondition;

@Extension
@HandlerId("loot/condition/world_regex")
public class WorldRegexConditionSerializer extends KeyedJsonSerializationHandler<JsonString, WorldRegexCondition> {

    public WorldRegexConditionSerializer(BasePlugin<?> plugin) {
        super(plugin, "pattern", STRING, WorldRegexCondition.class);
    }

    @Override
    public JsonString toJson(WorldRegexCondition value) {
        return IJson.of(value.stringPattern());
    }

    @Override
    public WorldRegexCondition fromJson(JsonString json) {
        return new WorldRegexCondition(json.value(), Pattern.compile(json.value()).asMatchPredicate());
    }

}
