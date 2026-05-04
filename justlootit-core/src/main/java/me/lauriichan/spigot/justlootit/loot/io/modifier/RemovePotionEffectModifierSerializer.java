package me.lauriichan.spigot.justlootit.loot.io.modifier;

import me.lauriichan.laylib.json.JsonString;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.io.LootIO;
import me.lauriichan.spigot.justlootit.loot.io.LootRegistry;
import me.lauriichan.spigot.justlootit.loot.modifier.RemovePotionEffectModifier;

@Extension
@HandlerId("loot/modifier/remove_potion_effect")
public class RemovePotionEffectModifierSerializer extends KeyedJsonSerializationHandler<JsonString, RemovePotionEffectModifier> {

    public RemovePotionEffectModifierSerializer(BasePlugin<?> plugin) {
        super(plugin, "effect_type", STRING, RemovePotionEffectModifier.class);
    }

    @Override
    public JsonString toJson(RemovePotionEffectModifier value) {
        return LootIO.fromRegistry(value.effectType());
    }
    
    @Override
    public RemovePotionEffectModifier fromJson(JsonString json) {
        return new RemovePotionEffectModifier(LootIO.asRegistry(LootRegistry.REGISTRY.effect(), json.value(), null));
    }

}
