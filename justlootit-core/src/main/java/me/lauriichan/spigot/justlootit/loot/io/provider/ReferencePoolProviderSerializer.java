package me.lauriichan.spigot.justlootit.loot.io.provider;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.JsonString;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.provider.ReferencePoolProvider;

@Extension
@HandlerId("loot/provider/pool/reference")
public class ReferencePoolProviderSerializer extends KeyedJsonSerializationHandler<JsonString, ReferencePoolProvider> {

    private final Plugin plugin;

    public ReferencePoolProviderSerializer(BasePlugin<?> plugin) {
        super(plugin, "custom_loottable_id", STRING, ReferencePoolProvider.class);
        this.plugin = plugin;
    }

    @Override
    public ReferencePoolProvider fromJson(JsonString json) {
        return new ReferencePoolProvider(NamespacedKey.fromString(json.value(), plugin));
    }

    @Override
    public JsonString toJson(ReferencePoolProvider value) {
        return IJson.of(value.lootTableKey().toString());
    }

}
