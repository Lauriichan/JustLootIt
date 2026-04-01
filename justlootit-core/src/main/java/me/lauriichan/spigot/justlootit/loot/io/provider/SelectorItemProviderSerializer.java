package me.lauriichan.spigot.justlootit.loot.io.provider;

import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.ILootItemProvider;
import me.lauriichan.spigot.justlootit.loot.io.LootIO;
import me.lauriichan.spigot.justlootit.loot.provider.SelectorItemProvider;

@Extension
@HandlerId("loot/provider/item/selector")
public class SelectorItemProviderSerializer extends KeyedJsonSerializationHandler<JsonArray, SelectorItemProvider> {

    public SelectorItemProviderSerializer(BasePlugin<?> plugin) {
        super(plugin, "item_providers", ARRAY, SelectorItemProvider.class);
    }

    @Override
    public JsonArray toJson(SelectorItemProvider value) {
        return LootIO.fromWeighted(ioManager, "item_provider", value.providers());
    }

    @Override
    public SelectorItemProvider fromJson(JsonArray json) {
        return new SelectorItemProvider(LootIO.asWeighted(ioManager, json, "item_provider", ILootItemProvider.class));
    }

}
