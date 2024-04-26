package me.lauriichan.spigot.justlootit.nms.v1_20_R3.nbt;

import me.lauriichan.spigot.justlootit.nms.nbt.ICompoundTag;
import me.lauriichan.spigot.justlootit.nms.nbt.IListTag;
import me.lauriichan.spigot.justlootit.nms.nbt.NbtHelper;
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;

public final class NbtHelper1_20_R3 extends NbtHelper {

    @Override
    public ICompoundTag createCompound() {
        return new CompoundTag1_20_R3();
    }

    @Override
    public <T> IListTag<T> createList(TagType<T> type) {
        return new ListTag1_20_R3<>(type);
    }

}
