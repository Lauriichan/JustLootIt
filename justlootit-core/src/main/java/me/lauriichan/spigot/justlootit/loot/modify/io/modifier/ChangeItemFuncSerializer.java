package me.lauriichan.spigot.justlootit.loot.modify.io.modifier;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.loot.modify.modifier.ChangeItemFunc;
import me.lauriichan.spigot.justlootit.nms.nbt.NbtHelper;

@Extension
@HandlerId("loot/modifier_func/change_item")
public class ChangeItemFuncSerializer extends KeyedJsonSerializationHandler<JsonObject, ChangeItemFunc> {

    private final NbtHelper nbtHelper;

    public ChangeItemFuncSerializer(JustLootItPlugin plugin) {
        super(plugin, "item", OBJECT, ChangeItemFunc.class);
        this.nbtHelper = plugin.versionHandler().nbtHelper();
    }

    @Override
    public JsonObject toJson(ChangeItemFunc value) {
        return nbtHelper.asJson(nbtHelper.asTag(value.replacement()));
    }

    @Override
    public ChangeItemFunc fromJson(JsonObject json) {
        return new ChangeItemFunc(nbtHelper.asItem(nbtHelper.asCompoundTag(json)));
    }

}
