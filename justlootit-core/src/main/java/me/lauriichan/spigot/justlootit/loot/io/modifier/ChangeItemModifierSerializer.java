package me.lauriichan.spigot.justlootit.loot.io.modifier;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.loot.ILootItemProvider;
import me.lauriichan.spigot.justlootit.loot.modifier.ChangeItemModifier;

@Extension
@HandlerId("loot/modifier/change_item")
public class ChangeItemModifierSerializer extends KeyedJsonSerializationHandler<JsonObject, ChangeItemModifier> {

    public ChangeItemModifierSerializer(JustLootItPlugin plugin) {
        super(plugin, "item_provider", OBJECT, ChangeItemModifier.class);
    }

    @Override
    public JsonObject toJson(ChangeItemModifier value) {
        return JsonIO.serialize(ioManager, value.provider());
    }

    @Override
    public ChangeItemModifier fromJson(JsonObject json) {
        return new ChangeItemModifier(JsonIO.deserialize(ioManager, json, ILootItemProvider.class));
    }

}
