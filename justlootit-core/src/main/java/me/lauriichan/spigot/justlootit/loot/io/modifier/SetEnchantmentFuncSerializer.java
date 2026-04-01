package me.lauriichan.spigot.justlootit.loot.io.modifier;

import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.io.LootIO;
import me.lauriichan.spigot.justlootit.loot.modifier.SetEnchantmentFunc;

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
        Enchantment enchantment = LootIO.readRegistry(registry, buffer, "enchantment", null);
        int level = buffer.getAsInt("level", 1);
        return new SetEnchantmentFunc(enchantment, level);
    }

    @Override
    protected void serialize(JsonObject buffer, SetEnchantmentFunc value) {
        LootIO.writeRegistry(buffer, "enchantment", value.enchantment());
        buffer.put("level", value.level());
    }

}
