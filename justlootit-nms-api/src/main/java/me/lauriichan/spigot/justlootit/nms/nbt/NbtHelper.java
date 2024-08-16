package me.lauriichan.spigot.justlootit.nms.nbt;

import org.bukkit.inventory.ItemStack;

import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.laylib.json.JsonObject;

public abstract class NbtHelper {

    public abstract ICompoundTag createCompound();

    public abstract <T> IListTag<T> createList(TagType<T> type);

    /*
     * To tag
     */

    public abstract ICompoundTag asTag(ItemStack itemStack);

    public final ICompoundTag asCompoundTag(JsonObject json) {
        return JsonNbtHelper.asCompoundTag(this, json);
    }

    public final IListTag<?> asListTag(JsonObject json) {
        return JsonNbtHelper.asListTag(this, json);
    }

    public final <T> IListTag<T> asListTag(TagType<T> type, JsonArray json) {
        return JsonNbtHelper.asListTag(this, type, json);
    }

    /*
     * From tag
     */

    public abstract ItemStack asItem(ICompoundTag tag);

    public final JsonObject asJson(ICompoundTag tag) {
        return JsonNbtHelper.asJson(tag);
    }

    public final JsonObject asJson(IListTag<?> tag) {
        return JsonNbtHelper.asJson(tag);
    }

}
