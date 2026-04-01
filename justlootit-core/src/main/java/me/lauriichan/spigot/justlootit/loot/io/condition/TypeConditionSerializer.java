package me.lauriichan.spigot.justlootit.loot.io.condition;

import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.JsonString;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.data.ContainerType;
import me.lauriichan.spigot.justlootit.loot.condition.TypeCondition;

@Extension
@HandlerId("loot/condition/container_type")
public class TypeConditionSerializer extends KeyedJsonSerializationHandler<JsonString, TypeCondition> {

    public TypeConditionSerializer(BasePlugin<?> plugin) {
        super(plugin, "container_type", STRING, TypeCondition.class);
    }

    @Override
    public JsonString toJson(TypeCondition value) {
        return IJson.of(value.type().name().toLowerCase());
    }

    @Override
    public TypeCondition fromJson(JsonString json) {
        try {
            return new TypeCondition(ContainerType.valueOf(json.value().toUpperCase()));
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("Unknown container type '%s'".formatted(json.value()));
        }
    }

}
