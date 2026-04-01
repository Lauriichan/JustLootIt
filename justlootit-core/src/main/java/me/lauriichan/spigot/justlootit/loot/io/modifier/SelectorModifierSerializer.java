package me.lauriichan.spigot.justlootit.loot.io.modifier;

import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.ILootModifier;
import me.lauriichan.spigot.justlootit.loot.io.LootIO;
import me.lauriichan.spigot.justlootit.loot.modifier.SelectorModifier;

@Extension
@HandlerId("loot/modifier/selector")
public class SelectorModifierSerializer extends KeyedJsonSerializationHandler<JsonArray, SelectorModifier> {

    public SelectorModifierSerializer(BasePlugin<?> plugin) {
        super(plugin, "modifiers", ARRAY, SelectorModifier.class);
    }

    @Override
    public JsonArray toJson(SelectorModifier value) {
        return LootIO.fromWeighted(ioManager, "modifier", value.functions());
    }

    @Override
    public SelectorModifier fromJson(JsonArray json) {
        return new SelectorModifier(LootIO.asWeighted(ioManager, json, "modifier", ILootModifier.class));
    }

}
