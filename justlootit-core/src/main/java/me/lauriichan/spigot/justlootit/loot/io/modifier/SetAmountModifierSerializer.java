package me.lauriichan.spigot.justlootit.loot.io.modifier;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.modifier.SetAmountModifier;

@Extension
@HandlerId("loot/modifier/set_amount")
public class SetAmountModifierSerializer extends JsonSerializationHandler<SetAmountModifier> {

    public SetAmountModifierSerializer(BasePlugin<?> plugin) {
        super(plugin, SetAmountModifier.class);
    }

    @Override
    public SetAmountModifier deserialize(JsonObject buffer) {
        int min = buffer.getAsInt("min", 0);
        int max = buffer.getAsInt("max", 64);
        return new SetAmountModifier(Math.min(min, max), Math.max(min, max));
    }

    @Override
    protected void serialize(JsonObject buffer, SetAmountModifier value) {
        buffer.put("min", value.min());
        buffer.put("max", value.max());
    }

}
