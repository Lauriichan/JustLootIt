package me.lauriichan.spigot.justlootit.loot.io.filter;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;

import me.lauriichan.laylib.json.JsonString;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.filter.MaterialFilter;
import me.lauriichan.spigot.justlootit.loot.io.LootIO;

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
        return LootIO.fromRegistry(value.material());
    }

    @Override
    public MaterialFilter fromJson(JsonString json) {
        return new MaterialFilter(LootIO.asRegistry(registry, json.value(), null));
    }

}
