package me.lauriichan.spigot.justlootit.loot.io.modifier;

import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.loot.ILootModifier;
import me.lauriichan.spigot.justlootit.loot.modifier.CombinedModifier;

@Extension
@HandlerId("loot/modifier/combined")
public class CombinedModifierSerializer extends KeyedJsonSerializationHandler<JsonArray, CombinedModifier> {

    public CombinedModifierSerializer(BasePlugin<?> plugin) {
        super(plugin, "modifiers", ARRAY, CombinedModifier.class);
    }

    @Override
    public JsonArray toJson(CombinedModifier value) {
        return JsonIO.serialize(ioManager, value.functions());
    }

    @Override
    public CombinedModifier fromJson(JsonArray json) {
        return new CombinedModifier(JsonIO.deserialize(ioManager, json, ILootModifier.class));
    }

}
