package me.lauriichan.spigot.justlootit.loot.io.modifier;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.loot.ILootModifierFunc;
import me.lauriichan.spigot.justlootit.loot.modifier.ChancedFunc;

@Extension
@HandlerId("loot/modifier_func/chanced")
public class ChancedFuncSerializer extends JsonSerializationHandler<ChancedFunc> {

    public ChancedFuncSerializer(BasePlugin<?> plugin) {
        super(plugin, ChancedFunc.class);
    }

    @Override
    public ChancedFunc deserialize(JsonObject buffer) {
        int threshold = Math.max(0, buffer.getAsInt("threshold", 25));
        int bound = Math.max(threshold, buffer.getAsInt("upper_bound", 100));
        return new ChancedFunc(JsonIO.deserialize(ioManager, buffer.getAsObject("function"), ILootModifierFunc.class), threshold, bound);
    }

    @Override
    protected void serialize(JsonObject buffer, ChancedFunc value) {
        buffer.put("function", JsonIO.serialize(ioManager, value.func()));
        buffer.put("threshold", value.threshold());
        buffer.put("upper_bound", value.bound());
    }

}
