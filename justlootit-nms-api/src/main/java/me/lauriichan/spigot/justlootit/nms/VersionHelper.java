package me.lauriichan.spigot.justlootit.nms;

import java.util.List;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.Lootable;

import me.lauriichan.spigot.justlootit.nms.model.IEntityData;
import net.md_5.bungee.api.chat.ItemTag;

public abstract class VersionHelper {
    
    public abstract VersionHandler handler();

    public void fillLoot(PlayerAdapter player, Lootable lootable) {
        if (lootable instanceof BlockState state) {
            fillLoot(player, state);
            return;
        }
        if (lootable instanceof Minecart minecart) {
            fillLoot(player, minecart);
            return;
        }
    }

    protected abstract void fillLoot(PlayerAdapter player, BlockState state);

    protected abstract void fillLoot(PlayerAdapter player, Minecart entity);
    
    public abstract ItemTag asItemTag(ItemStack itemStack);

    public abstract net.md_5.bungee.api.chat.hover.content.Entity createEntityHover(Entity entity);
    
    public abstract List<IEntityData> getEntityData(Entity entity);
    
}
