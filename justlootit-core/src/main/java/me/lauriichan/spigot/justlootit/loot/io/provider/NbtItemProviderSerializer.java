package me.lauriichan.spigot.justlootit.loot.io.provider;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.loot.provider.NbtItemProvider;
import me.lauriichan.spigot.justlootit.nms.nbt.NbtHelper;

@Extension
@HandlerId("loot/provider/item/nbt")
public class NbtItemProviderSerializer extends KeyedJsonSerializationHandler<JsonObject, NbtItemProvider> {

    private final NbtHelper nbtHelper;

    public NbtItemProviderSerializer(JustLootItPlugin plugin) {
        super(plugin, "item", OBJECT, NbtItemProvider.class);
        this.nbtHelper = plugin.versionHandler().nbtHelper();
    }

    @Override
    public JsonObject toJson(NbtItemProvider value) {
        return nbtHelper.asJson(nbtHelper.asTag(value.parsedStack()));
    }

    @Override
    public NbtItemProvider fromJson(JsonObject json) {
        return new NbtItemProvider(nbtHelper.asItem(nbtHelper.asCompoundTag(json)));
    }

}
