package me.lauriichan.spigot.justlootit.loot.io.condition;

import org.bukkit.NamespacedKey;

import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.JsonString;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.condition.LootTableCondition;

@Extension
@HandlerId("loot/condition/loottable")
public class LootTableConditionSerializer extends KeyedJsonSerializationHandler<JsonString, LootTableCondition> {

    public LootTableConditionSerializer(BasePlugin<?> plugin) {
        super(plugin, "namespaced_key", STRING, LootTableCondition.class);
    }

    @Override
    public JsonString toJson(LootTableCondition value) {
        return IJson.of(value.key().toString());
    }

    @Override
    public LootTableCondition fromJson(JsonString json) {
        return new LootTableCondition(NamespacedKey.fromString(json.value()));
    }

}
