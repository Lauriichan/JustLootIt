package me.lauriichan.spigot.justlootit.loot;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;

@Extension
@HandlerId("loot/")
public class _TempSerializer extends JsonSerializationHandler<Object> {

    public _TempSerializer(BasePlugin<?> plugin) {
        super(plugin, Object.class);
    }

    @Override
    public Object deserialize(JsonObject buffer) {
        return null;
    }

    @Override
    protected void serialize(JsonObject buffer, Object value) {
    }

}
