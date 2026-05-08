package me.lauriichan.spigot.justlootit.loot.io.modifier;

import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.JsonString;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.modifier.SetPotionColorModifier;

@Extension
@HandlerId("loot/modifier/set_potion_color")
public class SetPotionColorModifierSerializer extends KeyedJsonSerializationHandler<JsonString, SetPotionColorModifier> {
    
    public SetPotionColorModifierSerializer(BasePlugin<?> plugin) {
        super(plugin, "color", STRING, SetPotionColorModifier.class);
    }

    @Override
    public JsonString toJson(SetPotionColorModifier value) {
        return IJson.of(value.colorString());
    }

    @Override
    public SetPotionColorModifier fromJson(JsonString json) {
        return new SetPotionColorModifier(json.value());
    }

}
