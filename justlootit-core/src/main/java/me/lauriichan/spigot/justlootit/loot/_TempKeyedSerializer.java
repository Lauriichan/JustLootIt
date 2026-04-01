package me.lauriichan.spigot.justlootit.loot;

import me.lauriichan.laylib.json.JsonString;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;

@HandlerId("loot/")
public class _TempKeyedSerializer extends KeyedJsonSerializationHandler<JsonString, Object> {

    public _TempKeyedSerializer(BasePlugin<?> plugin) {
        super(plugin, "key", STRING, Object.class);
    }

    @Override
    public JsonString toJson(Object value) {
        return null;
    }

    @Override
    public Object fromJson(JsonString json) {
        return null;
    }

}
