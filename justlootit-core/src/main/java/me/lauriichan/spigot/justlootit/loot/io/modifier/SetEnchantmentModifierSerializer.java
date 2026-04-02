package me.lauriichan.spigot.justlootit.loot.io.modifier;

import org.bukkit.enchantments.Enchantment;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.io.LootIO;
import me.lauriichan.spigot.justlootit.loot.io.LootRegistry;
import me.lauriichan.spigot.justlootit.loot.modifier.SetEnchantmentModifier;

@Extension
@HandlerId("loot/modifier/set_enchantment")
public class SetEnchantmentModifierSerializer extends JsonSerializationHandler<SetEnchantmentModifier> {
    
    public SetEnchantmentModifierSerializer(BasePlugin<?> plugin) {
        super(plugin, SetEnchantmentModifier.class);
    }

    @Override
    public SetEnchantmentModifier deserialize(JsonObject buffer) {
        Enchantment enchantment = LootIO.readRegistry(LootRegistry.REGISTRY.enchantment(), buffer, "enchantment", null);
        int level = buffer.getAsInt("level", 1);
        return new SetEnchantmentModifier(enchantment, level);
    }

    @Override
    protected void serialize(JsonObject buffer, SetEnchantmentModifier value) {
        LootIO.writeRegistry(buffer, "enchantment", value.enchantment());
        buffer.put("level", value.level());
    }

}
