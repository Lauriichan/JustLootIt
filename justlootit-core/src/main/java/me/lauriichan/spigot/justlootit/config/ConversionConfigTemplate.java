package me.lauriichan.spigot.justlootit.config;

import me.lauriichan.minecraft.pluginbase.config.Config;
import me.lauriichan.minecraft.pluginbase.config.ConfigValue;
import me.lauriichan.minecraft.pluginbase.config.IConfigExtension;
import me.lauriichan.minecraft.pluginbase.config.IConfigHandler;
import me.lauriichan.minecraft.pluginbase.config.handler.JsonConfigHandler;
import me.lauriichan.minecraft.pluginbase.extension.Extension;

@Extension
@Config(automatic = true)
public class ConversionConfigTemplate implements IConfigExtension {
    
    @ConfigValue("convert.compatibility.lootin")
    public boolean doLootinConversion = false;

    @ConfigValue("convert.vanilla.do-conversion")
    public boolean doVanillaConversion = false;
    @ConfigValue("convert.vanilla.allow-static")
    public boolean allowStaticConversion = false;
    @ConfigValue("convert.vanilla.allow-item-frames")
    public boolean allowItemFrameConversion = false;
    
    /*
     * Implementation
     */

    @Override
    public IConfigHandler handler() {
        return JsonConfigHandler.JSON;
    }

    @Override
    public String path() {
        return "data://conversion.json";
    }

}
