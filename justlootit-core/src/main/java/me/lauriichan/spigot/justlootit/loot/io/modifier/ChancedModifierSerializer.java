package me.lauriichan.spigot.justlootit.loot.io.modifier;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.loot.ILootModifier;
import me.lauriichan.spigot.justlootit.loot.modifier.ChancedModifier;

@Extension
@HandlerId("loot/modifier/chanced")
public class ChancedModifierSerializer extends JsonSerializationHandler<ChancedModifier> {

    public ChancedModifierSerializer(BasePlugin<?> plugin) {
        super(plugin, ChancedModifier.class);
    }

    @Override
    public ChancedModifier deserialize(JsonObject buffer) {
        int threshold = Math.max(0, buffer.getAsInt("threshold", 25));
        int bound = Math.max(threshold, buffer.getAsInt("upper_bound", 100));
        return new ChancedModifier(JsonIO.deserialize(ioManager, buffer.getAsObject("modifier"), ILootModifier.class), threshold, bound);
    }

    @Override
    protected void serialize(JsonObject buffer, ChancedModifier value) {
        buffer.put("modifier", JsonIO.serialize(ioManager, value.func()));
        buffer.put("threshold", value.threshold());
        buffer.put("upper_bound", value.bound());
    }

}
