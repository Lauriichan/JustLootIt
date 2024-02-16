package me.lauriichan.spigot.justlootit.nms;

import java.io.File;
import java.util.concurrent.ExecutorService;

import org.bukkit.plugin.Plugin;

import me.lauriichan.laylib.logger.ISimpleLogger;

public interface IServiceProvider {

    Plugin plugin();

    ISimpleLogger logger();

    ExecutorService mainService();

    ExecutorService asyncService();
    
    File mainWorldFolder();

}
