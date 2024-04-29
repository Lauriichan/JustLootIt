package me.lauriichan.spigot.justlootit.config;

import me.lauriichan.minecraft.pluginbase.config.Config;
import me.lauriichan.minecraft.pluginbase.config.ConfigValue;
import me.lauriichan.minecraft.pluginbase.config.IConfigExtension;
import me.lauriichan.minecraft.pluginbase.config.IConfigHandler;
import me.lauriichan.minecraft.pluginbase.config.handler.JsonConfigHandler;
import me.lauriichan.minecraft.pluginbase.extension.Extension;

@Extension
@Config(automatic = true)
public class MainConfigTemplate implements IConfigExtension {
    
    @ConfigValue("player.cache.size")
    public int cacheSize = 20;
    
    @ConfigValue("player.cache.timeout-days")
    public int days = 7;
    
    @ConfigValue("container.delete-on-break")
    public boolean deleteOnBreak = true;
    
    /*
     * Implementation
     */

    @Override
    public IConfigHandler handler() {
        return JsonConfigHandler.JSON;
    }

    @Override
    public String path() {
        return "data://config.json";
    }
    
    /*
     * Validators
     */
    
    @ConfigValue("player.cache.size")
    public int validateConfigSize(int value) {
        return Math.max(Math.min(value, 48), 1);
    }
    
    @ConfigValue("player.cache.timeout-days")
    public int validateCacheTimeoutDays(int days) {
        return Math.max(days, 0);
    }

}
