package me.lauriichan.spigot.justlootit.loot.io.modifier;

import org.bukkit.enchantments.Enchantment;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.io.LootIO;
import me.lauriichan.spigot.justlootit.loot.io.LootRegistry;
import me.lauriichan.spigot.justlootit.loot.modifier.AddEnchantmentModifier;

@Extension
@HandlerId("loot/modifier/add_enchantment")
public class AddEnchantmentModifierSerializer extends JsonSerializationHandler<AddEnchantmentModifier> {

    public AddEnchantmentModifierSerializer(BasePlugin<?> plugin) {
        super(plugin, AddEnchantmentModifier.class);
    }

    @Override
    public AddEnchantmentModifier deserialize(JsonObject buffer) {
        Enchantment enchantment = LootIO.readRegistry(LootRegistry.REGISTRY.enchantment(), buffer, "enchantment", null);
        int level = buffer.getAsInt("level", 1);
        int minLevel = Math.max(0, buffer.getAsInt("min_level", 0));
        int maxLevel = Math.max(0, buffer.getAsInt("max_level", 255));
        boolean ignoreRestrictions = buffer.getAsBoolean("ignore_restrictions", false);
        return new AddEnchantmentModifier(enchantment, level, Math.min(minLevel, maxLevel), Math.max(minLevel, maxLevel), ignoreRestrictions);
    }

    @Override
    protected void serialize(JsonObject buffer, AddEnchantmentModifier value) {
        LootIO.writeRegistry(buffer, "enchantment", value.enchantment());
        buffer.put("level", value.level());
        buffer.put("min_level", value.minLevel());
        buffer.put("max_level", value.maxLevel());
        buffer.put("ignore_restrictions", value.ignoreRestrictions());
    }

}
