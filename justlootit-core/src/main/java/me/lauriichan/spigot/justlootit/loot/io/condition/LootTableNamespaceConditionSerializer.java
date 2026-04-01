package me.lauriichan.spigot.justlootit.loot.io.condition;

import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.JsonString;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.condition.LootTableNamespaceCondition;

@Extension
@HandlerId("loot/condition/loottable_namespace")
public class LootTableNamespaceConditionSerializer extends KeyedJsonSerializationHandler<JsonString, LootTableNamespaceCondition> {

    public LootTableNamespaceConditionSerializer(BasePlugin<?> plugin) {
        super(plugin, "namespace", STRING, LootTableNamespaceCondition.class);
    }

    @Override
    public JsonString toJson(LootTableNamespaceCondition value) {
        return IJson.of(value.namespace());
    }

    @Override
    public LootTableNamespaceCondition fromJson(JsonString json) {
        return new LootTableNamespaceCondition(json.value());
    }

}
