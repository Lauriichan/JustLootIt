package me.lauriichan.spigot.justlootit.loot.io.provider;

import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.loot.ILootPoolProvider;
import me.lauriichan.spigot.justlootit.loot.provider.CombinedPoolProvider;

@Extension
@HandlerId("loot/provider/pool/combined")
public class CombinedPoolProviderSerializer extends KeyedJsonSerializationHandler<JsonArray, CombinedPoolProvider> {

    public CombinedPoolProviderSerializer(BasePlugin<?> plugin) {
        super(plugin, "pool_providers", ARRAY, CombinedPoolProvider.class);
    }

    @Override
    public JsonArray toJson(CombinedPoolProvider value) {
        return JsonIO.serialize(ioManager, value.providers());
    }

    @Override
    public CombinedPoolProvider fromJson(JsonArray json) {
        return new CombinedPoolProvider(JsonIO.deserialize(ioManager, json, ILootPoolProvider.class));
    }

}
