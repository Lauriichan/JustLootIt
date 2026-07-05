package me.lauriichan.spigot.justlootit.loot.io.provider;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.JsonString;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.provider.ReferenceItemProvider;

@Extension
@HandlerId("loot/provider/item/reference")
public class ReferenceItemProviderSerializer extends KeyedJsonSerializationHandler<JsonString, ReferenceItemProvider> {

    private final Plugin plugin;

    public ReferenceItemProviderSerializer(BasePlugin<?> plugin) {
        super(plugin, "custom_loottable_id", STRING, ReferenceItemProvider.class);
        this.plugin = plugin;
    }

    @Override
    public ReferenceItemProvider fromJson(JsonString json) {
        return new ReferenceItemProvider(NamespacedKey.fromString(json.value(), plugin));
    }

    @Override
    public JsonString toJson(ReferenceItemProvider value) {
        return IJson.of(value.lootTableKey().toString());
    }

}
