package me.lauriichan.spigot.justlootit.loot.io.modifier;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.modifier.ClearEnchantmentsModifier;

@Extension
@HandlerId("loot/modifier/clear_enchantments")
public class ClearEnchantmentsModifierSerializer extends JsonSerializationHandler<ClearEnchantmentsModifier> {

    public ClearEnchantmentsModifierSerializer(BasePlugin<?> plugin) {
        super(plugin, ClearEnchantmentsModifier.class);
    }

    @Override
    public ClearEnchantmentsModifier deserialize(JsonObject buffer) {
        return new ClearEnchantmentsModifier();
    }

    @Override
    protected void serialize(JsonObject buffer, ClearEnchantmentsModifier value) {}

}
