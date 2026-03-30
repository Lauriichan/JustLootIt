package me.lauriichan.spigot.justlootit.loot.modify.io.modifier;

import me.lauriichan.laylib.json.JsonString;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.modify.modifier.SetNameFunc;

@Extension
@HandlerId("loot/modifier_func/set_name")
public class SetNameFuncSerializer extends KeyedJsonSerializationHandler<JsonString, SetNameFunc> {

    public SetNameFuncSerializer(BasePlugin<?> plugin) {
        super(plugin, "new_name", STRING, SetNameFunc.class);
    }

    @Override
    public JsonString toJson(SetNameFunc value) {
        return new JsonString(value.newNameText());
    }

    @Override
    public SetNameFunc fromJson(JsonString json) {
        return new SetNameFunc(json.value());
    }

}
