package me.lauriichan.spigot.justlootit.loot.io.condition;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.loot.ILootCondition;
import me.lauriichan.spigot.justlootit.loot.condition.NotCondition;

@Extension
@HandlerId("loot/condition/not")
public class NotConditionSerializer extends KeyedJsonSerializationHandler<JsonObject, NotCondition> {

    public NotConditionSerializer(BasePlugin<?> plugin) {
        super(plugin, "condition", OBJECT, NotCondition.class);
    }

    @Override
    public JsonObject toJson(NotCondition value) {
        return JsonIO.serialize(ioManager, value.condition());
    }

    @Override
    public NotCondition fromJson(JsonObject json) {
        return new NotCondition(JsonIO.deserialize(ioManager, json, ILootCondition.class));
    }

}
