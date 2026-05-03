package me.lauriichan.spigot.justlootit.loot.io.modifier;

import org.bukkit.potion.PotionEffectType;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.io.LootIO;
import me.lauriichan.spigot.justlootit.loot.io.LootRegistry;
import me.lauriichan.spigot.justlootit.loot.modifier.RemovePotionEffectModifier;

@Extension
@HandlerId("loot/modifier/remove_potion_effect")
public class RemovePotionEffectModifierSerializer extends JsonSerializationHandler<RemovePotionEffectModifier> {

    public RemovePotionEffectModifierSerializer(BasePlugin<?> plugin) {
        super(plugin, RemovePotionEffectModifier.class);
    }

    @Override
    public RemovePotionEffectModifier deserialize(JsonObject buffer) {
        PotionEffectType effectType = LootIO.readRegistry(LootRegistry.REGISTRY.effect(), buffer, "effect_type", null);
        return new RemovePotionEffectModifier(effectType);
    }

    @Override
    protected void serialize(JsonObject buffer, RemovePotionEffectModifier value) {
        LootIO.writeRegistry(buffer, "effect_type", value.effectType());
    }

}
