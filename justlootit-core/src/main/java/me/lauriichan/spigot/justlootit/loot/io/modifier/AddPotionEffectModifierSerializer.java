package me.lauriichan.spigot.justlootit.loot.io.modifier;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.io.LootIO;
import me.lauriichan.spigot.justlootit.loot.io.LootRegistry;
import me.lauriichan.spigot.justlootit.loot.modifier.AddPotionEffectModifier;

@Extension
@HandlerId("loot/modifier/add_potion_effect")
public class AddPotionEffectModifierSerializer extends JsonSerializationHandler<AddPotionEffectModifier> {

    public AddPotionEffectModifierSerializer(BasePlugin<?> plugin) {
        super(plugin, AddPotionEffectModifier.class);
    }

    @Override
    public AddPotionEffectModifier deserialize(JsonObject buffer) {
        PotionEffectType effectType = LootIO.readRegistry(LootRegistry.REGISTRY.effect(), buffer, "effect_type", null);
        int duration = Math.max(1, buffer.getAsInt("duration", 600));
        int amplifier = Math.min(Math.max(0, buffer.getAsInt("amplifier", 0)), 255);
        boolean ambient = buffer.getAsBoolean("ambient", true);
        boolean particles = buffer.getAsBoolean("particles", true);
        boolean icon = buffer.getAsBoolean("icon", true);
        boolean overwrites = buffer.getAsBoolean("overwrites_existing", false);
        return new AddPotionEffectModifier(new PotionEffect(effectType, duration, amplifier, ambient, particles, icon), overwrites);
    }

    @Override
    protected void serialize(JsonObject buffer, AddPotionEffectModifier value) {
        PotionEffect effect = value.effect();
        LootIO.writeRegistry(buffer, "effect_type", effect.getType());
        buffer.put("duration", effect.getDuration());
        buffer.put("amplifier", effect.getAmplifier());
        buffer.put("ambient", effect.isAmbient());
        buffer.put("has_particles", effect.hasParticles());
        buffer.put("has_icon", effect.hasIcon());
        buffer.put("overwrites_existing", value.overwrite());
    }

}
