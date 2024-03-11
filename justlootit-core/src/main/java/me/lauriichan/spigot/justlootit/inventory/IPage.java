package me.lauriichan.spigot.justlootit.inventory;

import me.lauriichan.minecraft.pluginbase.inventory.paged.IInventoryPageExtension;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;

public interface IPage<P extends IPage<P>> extends IInventoryPageExtension<P, PlayerAdapter> {

}
