package me.lauriichan.spigot.justlootit.loot.io.modifier;

import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.ILootModifierFunc;
import me.lauriichan.spigot.justlootit.loot.io.LootIO;
import me.lauriichan.spigot.justlootit.loot.modifier.SelectorFunc;

@Extension
@HandlerId("loot/modifier_func/selector")
public class SelectorFuncSerializer extends KeyedJsonSerializationHandler<JsonArray, SelectorFunc> {

    public SelectorFuncSerializer(BasePlugin<?> plugin) {
        super(plugin, "functions", ARRAY, SelectorFunc.class);
    }

    @Override
    public JsonArray toJson(SelectorFunc value) {
        return LootIO.fromWeighted(ioManager, "function", value.functions());
    }

    @Override
    public SelectorFunc fromJson(JsonArray json) {
        return new SelectorFunc(LootIO.asWeighted(ioManager, json, "function", ILootModifierFunc.class));
    }

}
