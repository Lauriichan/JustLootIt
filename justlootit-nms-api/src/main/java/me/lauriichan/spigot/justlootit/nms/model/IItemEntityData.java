package me.lauriichan.spigot.justlootit.nms.model;

import org.bukkit.inventory.ItemStack;

public interface IItemEntityData extends IEntityData {

    ItemStack getItem();

    void setItem(ItemStack itemStack);

}
