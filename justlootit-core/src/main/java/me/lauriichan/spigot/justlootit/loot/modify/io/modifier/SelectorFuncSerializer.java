package me.lauriichan.spigot.justlootit.loot.modify.io.modifier;

import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.loot.modify.ILootModifierFunc;
import me.lauriichan.spigot.justlootit.loot.modify.modifier.SelectorFunc;
import me.lauriichan.spigot.justlootit.util.WeightedList;

@Extension
@HandlerId("loot/modifier_func/selector")
public class SelectorFuncSerializer extends KeyedJsonSerializationHandler<JsonArray, SelectorFunc> {

    public SelectorFuncSerializer(BasePlugin<?> plugin) {
        super(plugin, "filters", ARRAY, SelectorFunc.class);
    }

    @Override
    public JsonArray toJson(SelectorFunc value) {
        JsonArray array = new JsonArray();
        for (WeightedList.Entry<ILootModifierFunc> entry : value.functions()) {
            JsonObject object = new JsonObject();
            object.put("weight", entry.weight());
            object.put("function", JsonIO.serialize(ioManager, entry.element()));
            array.add(object);
        }
        return array;
    }

    @Override
    public SelectorFunc fromJson(JsonArray json) {
        WeightedList<ILootModifierFunc> functions = new WeightedList<>();
        for (IJson<?> entry : json) {
            if (!entry.isObject()) {
                continue;
            }
            JsonObject object = entry.asJsonObject();
            double weight = object.getAsDouble("weight", 1d);
            ILootModifierFunc function = JsonIO.deserialize(ioManager, object.getAsObject("function"), ILootModifierFunc.class);
            functions.add(weight, function);
        }
        return new SelectorFunc(functions);
    }

}
