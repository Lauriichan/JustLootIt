package me.lauriichan.spigot.justlootit.loot.modify.io.modifier;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.modify.modifier.SetAmountFunc;

@Extension
@HandlerId("loot/modifier_func/set_amount")
public class SetAmountFuncSerializer extends JsonSerializationHandler<SetAmountFunc> {

    public SetAmountFuncSerializer(BasePlugin<?> plugin) {
        super(plugin, SetAmountFunc.class);
    }

    @Override
    public SetAmountFunc deserialize(JsonObject buffer) {
        int min = buffer.getAsInt("min", 0);
        int max = buffer.getAsInt("max", 64);
        return new SetAmountFunc(Math.min(min, max), Math.max(min, max));
    }

    @Override
    protected void serialize(JsonObject buffer, SetAmountFunc value) {
        buffer.put("min", value.min());
        buffer.put("max", value.max());
    }

}
