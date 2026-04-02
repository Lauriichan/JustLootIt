package me.lauriichan.spigot.justlootit.loot.io.provider;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.loot.ILootPoolProvider;
import me.lauriichan.spigot.justlootit.loot.provider.ChancedPoolProvider;

@Extension
@HandlerId("loot/provider/pool/chanced")
public class ChancedPoolProviderSerializer extends JsonSerializationHandler<ChancedPoolProvider> {

    public ChancedPoolProviderSerializer(BasePlugin<?> plugin) {
        super(plugin, ChancedPoolProvider.class);
    }

    @Override
    public ChancedPoolProvider deserialize(JsonObject buffer) {
        int threshold = Math.max(0, buffer.getAsInt("threshold", 25));
        int bound = Math.max(threshold, buffer.getAsInt("upper_bound", 100));
        return new ChancedPoolProvider(JsonIO.deserialize(ioManager, buffer.getAsObject("pool_provider"), ILootPoolProvider.class), threshold, bound);
    }

    @Override
    protected void serialize(JsonObject buffer, ChancedPoolProvider value) {
        buffer.put("pool_provider", JsonIO.serialize(ioManager, value.poolProvider()));
        buffer.put("threshold", value.threshold());
        buffer.put("upper_bound", value.bound());
    }

}
