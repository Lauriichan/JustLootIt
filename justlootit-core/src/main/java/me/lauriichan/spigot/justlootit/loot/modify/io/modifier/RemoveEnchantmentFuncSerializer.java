package me.lauriichan.spigot.justlootit.loot.modify.io.modifier;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;

import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.JsonString;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.modify.modifier.RemoveEnchantmentFunc;
import me.lauriichan.spigot.justlootit.nms.util.RegistryUtil;

@Extension
@HandlerId("loot/modifier_func/remove_enchantment")
public class RemoveEnchantmentFuncSerializer extends KeyedJsonSerializationHandler<JsonString, RemoveEnchantmentFunc> {

    private final Registry<Enchantment> registry;

    public RemoveEnchantmentFuncSerializer(BasePlugin<?> plugin) {
        super(plugin, "enchantment", STRING, RemoveEnchantmentFunc.class);
        this.registry = Bukkit.getRegistry(Enchantment.class);
    }

    @Override
    public JsonString toJson(RemoveEnchantmentFunc value) {
        return IJson.of(RegistryUtil.getKey(value.enchantment()).toString());
    }

    @Override
    public RemoveEnchantmentFunc fromJson(JsonString json) {
        return new RemoveEnchantmentFunc(registry.getOrThrow(NamespacedKey.fromString(json.value())));
    }

}
