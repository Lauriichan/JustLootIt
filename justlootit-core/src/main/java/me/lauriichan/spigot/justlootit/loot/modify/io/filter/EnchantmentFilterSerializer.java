package me.lauriichan.spigot.justlootit.loot.modify.io.filter;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.modify.filter.EnchantmentFilter;
import me.lauriichan.spigot.justlootit.nms.util.RegistryUtil;

@Extension
@HandlerId("loot/filter/enchantment")
public class EnchantmentFilterSerializer extends JsonSerializationHandler<EnchantmentFilter> {

    private final Registry<Enchantment> registry;

    public EnchantmentFilterSerializer(BasePlugin<?> plugin) {
        super(plugin, EnchantmentFilter.class);
        this.registry = Bukkit.getRegistry(Enchantment.class);
    }

    @Override
    public EnchantmentFilter deserialize(JsonObject buffer) {
        Enchantment enchantment = registry.getOrThrow(NamespacedKey.fromString(buffer.getAsString("enchantment")));
        int min = buffer.getAsInt("min_level", 0);
        int max = buffer.getAsInt("max_level", Integer.MAX_VALUE);
        return new EnchantmentFilter(enchantment, Math.min(min, max), Math.max(min, max));
    }

    @Override
    protected void serialize(JsonObject buffer, EnchantmentFilter value) {
        buffer.put("enchantment", RegistryUtil.getKey(value.enchantment()).toString());
        buffer.put("min_level", value.min());
        buffer.put("max_level", value.max());
    }

}
