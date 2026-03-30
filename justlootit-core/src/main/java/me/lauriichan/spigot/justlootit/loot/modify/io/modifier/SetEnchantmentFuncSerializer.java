package me.lauriichan.spigot.justlootit.loot.modify.io.modifier;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.modify.modifier.SetEnchantmentFunc;
import me.lauriichan.spigot.justlootit.nms.util.RegistryUtil;

@Extension
@HandlerId("loot/modifier_func/set_enchantment")
public class SetEnchantmentFuncSerializer extends JsonSerializationHandler<SetEnchantmentFunc> {

    private final Registry<Enchantment> registry;
    
    public SetEnchantmentFuncSerializer(BasePlugin<?> plugin) {
        super(plugin, SetEnchantmentFunc.class);
        this.registry = Bukkit.getRegistry(Enchantment.class);
    }

    @Override
    public SetEnchantmentFunc deserialize(JsonObject buffer) {
        Enchantment enchantment = registry.getOrThrow(NamespacedKey.fromString(buffer.getAsString("enchantment")));
        int level = buffer.getAsInt("level", 1);
        return new SetEnchantmentFunc(enchantment, level);
    }

    @Override
    protected void serialize(JsonObject buffer, SetEnchantmentFunc value) {
        buffer.put("enchantment", RegistryUtil.getKey(value.enchantment()).toString());
        buffer.put("level", value.level());
    }

}
