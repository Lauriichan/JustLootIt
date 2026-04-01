package me.lauriichan.spigot.justlootit.loot.io.provider;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.ILootPoolProvider;
import me.lauriichan.spigot.justlootit.loot.io.LootIO;
import me.lauriichan.spigot.justlootit.loot.provider.SelectorPoolProvider;
import me.lauriichan.spigot.justlootit.util.WeightedList;

@Extension
@HandlerId("loot/provider/pool/selector")
public class SelectorPoolProviderSerializer extends JsonSerializationHandler<SelectorPoolProvider> {

    public SelectorPoolProviderSerializer(BasePlugin<?> plugin) {
        super(plugin, SelectorPoolProvider.class);
    }

    @Override
    public SelectorPoolProvider deserialize(JsonObject buffer) {
        WeightedList<ILootPoolProvider> providers = LootIO.readWeighted(ioManager, buffer, "pool_providers", "pool_provider",
            ILootPoolProvider.class);
        int minRolls = Math.max(0, buffer.getAsInt("min_rolls", 0));
        int maxRolls = Math.max(1, buffer.getAsInt("max_rolls", 1));
        return new SelectorPoolProvider(providers, Math.min(minRolls, maxRolls), Math.max(minRolls, maxRolls));
    }

    @Override
    protected void serialize(JsonObject buffer, SelectorPoolProvider value) {
        LootIO.writeWeighted(ioManager, buffer, "pool_providers", "pool_provider", value.providers());
        buffer.put("min_rolls", value.minRolls());
        buffer.put("max_rolls", value.maxRolls());
    }

}
