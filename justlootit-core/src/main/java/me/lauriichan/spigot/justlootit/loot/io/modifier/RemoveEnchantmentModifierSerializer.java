package me.lauriichan.spigot.justlootit.loot.io.modifier;

import me.lauriichan.laylib.json.JsonString;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.io.LootIO;
import me.lauriichan.spigot.justlootit.loot.io.LootRegistry;
import me.lauriichan.spigot.justlootit.loot.modifier.RemoveEnchantmentModifier;

@Extension
@HandlerId("loot/modifier/remove_enchantment")
public class RemoveEnchantmentModifierSerializer extends KeyedJsonSerializationHandler<JsonString, RemoveEnchantmentModifier> {

    public RemoveEnchantmentModifierSerializer(BasePlugin<?> plugin) {
        super(plugin, "enchantment", STRING, RemoveEnchantmentModifier.class);
    }

    @Override
    public JsonString toJson(RemoveEnchantmentModifier value) {
        return LootIO.fromRegistry(value.enchantment());
    }

    @Override
    public RemoveEnchantmentModifier fromJson(JsonString json) {
        return new RemoveEnchantmentModifier(LootIO.asRegistry(LootRegistry.REGISTRY.enchantment(), json.value(), null));
    }

}
