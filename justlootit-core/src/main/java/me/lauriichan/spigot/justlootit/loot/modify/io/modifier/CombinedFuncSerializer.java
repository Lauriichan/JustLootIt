package me.lauriichan.spigot.justlootit.loot.modify.io.modifier;

import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.loot.modify.ILootModifierFunc;
import me.lauriichan.spigot.justlootit.loot.modify.modifier.CombinedFunc;

@Extension
@HandlerId("loot/modifier_func/combined")
public class CombinedFuncSerializer extends KeyedJsonSerializationHandler<JsonArray, CombinedFunc> {

    public CombinedFuncSerializer(BasePlugin<?> plugin) {
        super(plugin, "filters", ARRAY, CombinedFunc.class);
    }

    @Override
    public JsonArray toJson(CombinedFunc value) {
        return JsonIO.serialize(ioManager, value.functions());
    }

    @Override
    public CombinedFunc fromJson(JsonArray json) {
        return new CombinedFunc(JsonIO.deserialize(ioManager, json, ILootModifierFunc.class));
    }

}
