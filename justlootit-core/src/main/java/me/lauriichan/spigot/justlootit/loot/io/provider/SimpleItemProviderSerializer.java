package me.lauriichan.spigot.justlootit.loot.io.provider;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.io.LootIO;
import me.lauriichan.spigot.justlootit.loot.provider.SimpleItemProvider;

@Extension
@HandlerId("loot/provider/item/simple")
public class SimpleItemProviderSerializer extends JsonSerializationHandler<SimpleItemProvider> {

    private final Registry<Material> registry;

    public SimpleItemProviderSerializer(BasePlugin<?> plugin) {
        super(plugin, SimpleItemProvider.class);
        this.registry = Bukkit.getRegistry(Material.class);
    }

    @Override
    public SimpleItemProvider deserialize(JsonObject buffer) {
        Material material = LootIO.readRegistry(registry, buffer, "material", Material.STONE);
        int amount = Math.max(1, buffer.getAsInt("amount", 1));
        return new SimpleItemProvider(material, amount);
    }

    @Override
    protected void serialize(JsonObject buffer, SimpleItemProvider value) {
        LootIO.writeRegistry(buffer, "material", value.material());
        buffer.put("amount", value.amount());
    }

}
