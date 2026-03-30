package me.lauriichan.spigot.justlootit.loot.modify.io.filter;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.JsonString;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.modify.filter.MaterialFilter;
import me.lauriichan.spigot.justlootit.nms.util.RegistryUtil;

@Extension
@HandlerId("loot/filter/material")
public class MaterialFilterSerializer extends KeyedJsonSerializationHandler<JsonString, MaterialFilter> {

    private final Registry<Material> registry;

    public MaterialFilterSerializer(BasePlugin<?> plugin) {
        super(plugin, "item_material", STRING, MaterialFilter.class);
        this.registry = Bukkit.getRegistry(Material.class);
    }

    @Override
    public JsonString toJson(MaterialFilter value) {
        return IJson.of(RegistryUtil.getKey(value.material()).toString());
    }

    @Override
    public MaterialFilter fromJson(JsonString json) {
        return new MaterialFilter(registry.getOrThrow(NamespacedKey.fromString(json.value())));
    }

}
