package me.lauriichan.spigot.justlootit.nms;

import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.chat.ItemTag;

public abstract class VersionHelper {
    
    public abstract VersionHandler handler();
    
    public abstract ItemTag asItemTag(ItemStack itemStack);

    public abstract net.md_5.bungee.api.chat.hover.content.Entity createEntityHover(Entity entity);
    
}
