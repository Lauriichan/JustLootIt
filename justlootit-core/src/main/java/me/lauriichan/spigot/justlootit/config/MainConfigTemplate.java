package me.lauriichan.spigot.justlootit.config;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.config.Config;
import me.lauriichan.minecraft.pluginbase.config.ConfigValue;
import me.lauriichan.minecraft.pluginbase.config.ConfigValueValidator;
import me.lauriichan.minecraft.pluginbase.config.Configuration;
import me.lauriichan.minecraft.pluginbase.config.IConfigHandler;
import me.lauriichan.minecraft.pluginbase.config.ISingleConfigExtension;
import me.lauriichan.minecraft.pluginbase.config.handler.JsonConfigHandler;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.util.TypeName;

@Extension
@Config(automatic = true)
public class MainConfigTemplate implements ISingleConfigExtension {

    @ConfigValue("cache.player.size")
    public int playerInventoryCacheSize = 20;

    @ConfigValue("cache.player.timeout-inventory-days")
    public int playerInventoryDayTimeout = 7;

    @ConfigValue("cache.player.keep-in-memory")
    public long playerCacheKeepInMemory = 120L;

    @ConfigValue("cache.level.keep-file-open")
    public long levelCacheKeepFileOpen = 9000L;
    @ConfigValue("cache.level.keep-in-memory")
    public long levelCacheKeepInMemory = 600L;

    @ConfigValue("container.delete-on-break")
    public boolean deleteOnBreak = true;
    
    @ConfigValue("loot.unique")
    public boolean uniqueLootPerPlayer = false;
    
    private final VersionHandler versionHandler;
    
    public MainConfigTemplate(JustLootItPlugin plugin) {
        this.versionHandler = plugin.versionHandler();
    }

    /*
     * Implementation
     */
    
    @Override
    public String name() {
        return TypeName.ofConfig(this);
    }

    @Override
    public IConfigHandler handler() {
        return JsonConfigHandler.JSON;
    }

    @Override
    public String path() {
        return "data://config.json";
    }
    
    /*
     * On load
     */
    
    @Override
    public void onLoad(ISimpleLogger logger, Configuration configuration) throws Exception {
        updateStorages();
    }
    
    private void updateStorages() {
        MainConfig config = (MainConfig) ((Object) this);
        versionHandler.forEachPlayer(player -> player.getCapability(StorageCapability.class).ifPresent(storage -> storage.updateConfiguration(config)));
        versionHandler.forEachLevel(level -> level.getCapability(StorageCapability.class).ifPresent(storage -> storage.updateConfiguration(config)));
    }

    /*
     * Validators
     */

    @ConfigValueValidator("cache.player.size")
    public int validateConfigSize(int value) {
        return Math.max(Math.min(value, 48), 1);
    }

    @ConfigValueValidator("cache.player.timeout-inventory-days")
    public int validateCacheTimeoutDays(int days) {
        return Math.max(days, 0);
    }

    @ConfigValueValidator({
        "cache.player.keep-in-memory",
        "cache.level.keep-in-memory"
    })
    public long validateCacheKeepInMemory(long value) {
        return Math.max(Math.min(value, 3600L), 15L);
    }

}
