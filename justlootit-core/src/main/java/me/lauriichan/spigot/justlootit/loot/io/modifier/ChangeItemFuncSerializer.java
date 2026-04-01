package me.lauriichan.spigot.justlootit.loot.io.modifier;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.loot.ILootItemProvider;
import me.lauriichan.spigot.justlootit.loot.modifier.ChangeItemFunc;

@Extension
@HandlerId("loot/modifier_func/change_item")
public class ChangeItemFuncSerializer extends KeyedJsonSerializationHandler<JsonObject, ChangeItemFunc> {

    public ChangeItemFuncSerializer(JustLootItPlugin plugin) {
        super(plugin, "item_provider", OBJECT, ChangeItemFunc.class);
    }

    @Override
    public JsonObject toJson(ChangeItemFunc value) {
        return JsonIO.serialize(ioManager, value.provider());
    }

    @Override
    public ChangeItemFunc fromJson(JsonObject json) {
        return new ChangeItemFunc(JsonIO.deserialize(ioManager, json, ILootItemProvider.class));
    }

}
