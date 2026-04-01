package me.lauriichan.spigot.justlootit.listener;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldLoadEvent;

import me.lauriichan.minecraft.pluginbase.config.MultiConfigWrapper;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.listener.IListenerExtension;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.config.loot.LootConfig;
import me.lauriichan.spigot.justlootit.config.loot.LootMultiConfig;

@Extension
public class WorldListener implements IListenerExtension {

    private final MultiConfigWrapper<?, World, LootConfig, LootMultiConfig> lootMulti;

    public WorldListener(JustLootItPlugin plugin) {
        this.lootMulti = plugin.configManager().multiWrapper(LootMultiConfig.class);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        // Reload the loot config for this world
        lootMulti.wrapperOrCreate(event.getWorld()).reload();
    }

}
