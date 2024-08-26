package me.lauriichan.spigot.justlootit.api.event.player;

import org.bukkit.inventory.Inventory;

import me.lauriichan.maven.justlootit.annotation.Event;
import me.lauriichan.maven.justlootit.annotation.EventField;
import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventory;

@Event
public abstract class AsyncJLIPlayerLootProvidedEventTmp extends AsyncJLIPlayerEventTmp {
    
    @EventField
    private IGuiInventory inventory;
    
    public final Inventory bukkitInventory() {
        return inventory.getInventory();
    }

}
