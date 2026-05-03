package me.lauriichan.spigot.justlootit.loot.io.modifier;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.modifier.ClearPotionEffectsModifier;

@Extension
@HandlerId("loot/modifier/clear_potion_effects")
public class ClearPotionEffectsModifierSerializer extends JsonSerializationHandler<ClearPotionEffectsModifier> {

    public ClearPotionEffectsModifierSerializer(BasePlugin<?> plugin) {
        super(plugin, ClearPotionEffectsModifier.class);
    }

    @Override
    public ClearPotionEffectsModifier deserialize(JsonObject buffer) {
        return new ClearPotionEffectsModifier();
    }

    @Override
    protected void serialize(JsonObject buffer, ClearPotionEffectsModifier value) {}

}
