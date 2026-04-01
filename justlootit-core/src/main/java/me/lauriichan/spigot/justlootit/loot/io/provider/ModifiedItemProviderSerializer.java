package me.lauriichan.spigot.justlootit.loot.io.provider;

import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.loot.ILootItemProvider;
import me.lauriichan.spigot.justlootit.loot.ILootModifierFunc;
import me.lauriichan.spigot.justlootit.loot.provider.ModifiedItemProvider;

@Extension
@HandlerId("loot/provider/item/modify")
public class ModifiedItemProviderSerializer extends JsonSerializationHandler<ModifiedItemProvider> {

    public ModifiedItemProviderSerializer(BasePlugin<?> plugin) {
        super(plugin, ModifiedItemProvider.class);
    }

    @Override
    public ModifiedItemProvider deserialize(JsonObject buffer) {
        ILootItemProvider provider = JsonIO.deserialize(ioManager, buffer.getAsObject("item_provider"), ILootItemProvider.class);
        ObjectList<ILootModifierFunc> modifiers = JsonIO.deserialize(ioManager, buffer.getAsArray("modifiers"), ILootModifierFunc.class);
        return new ModifiedItemProvider(provider, modifiers);
    }

    @Override
    protected void serialize(JsonObject buffer, ModifiedItemProvider value) {
        buffer.put("item_provider", JsonIO.serialize(ioManager, value.itemProvider()));
        buffer.put("modifiers", JsonIO.serialize(ioManager, value.modifiers()));
    }

}
