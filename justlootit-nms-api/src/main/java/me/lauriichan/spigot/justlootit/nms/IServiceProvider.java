package me.lauriichan.spigot.justlootit.nms;

import java.io.File;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.platform.JustLootItPlatform;
import me.lauriichan.spigot.justlootit.platform.scheduler.Scheduler;

public interface IServiceProvider {

    Plugin plugin();

    ISimpleLogger logger();
    
    JustLootItPlatform platform();
    
    default Scheduler scheduler() {
        return platform().scheduler();
    }
    
    File mainWorldFolder();
    
    Actor<Player> playerActor(Player player);

}
