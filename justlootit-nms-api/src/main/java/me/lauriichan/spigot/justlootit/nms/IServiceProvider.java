package me.lauriichan.spigot.justlootit.nms;

import java.util.concurrent.ExecutorService;

import org.bukkit.plugin.Plugin;

public interface IServiceProvider {
    
    Plugin plugin();
    
    ExecutorService mainService();
    
    ExecutorService asyncService();

}
