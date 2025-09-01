package me.lauriichan.spigot.justlootit.compatibility.provider.customstructures;

import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import com.ryandw11.structure.api.LootPopulateEvent;

import me.lauriichan.minecraft.pluginbase.config.ConfigManager;
import me.lauriichan.spigot.justlootit.JustLootItAccess;
import me.lauriichan.spigot.justlootit.JustLootItConstant;
import me.lauriichan.spigot.justlootit.JustLootItFlag;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.compatibility.data.CompatibilityDataExtension;
import me.lauriichan.spigot.justlootit.compatibility.data.customstructures.CustomStructuresDataExtension;
import me.lauriichan.spigot.justlootit.config.MainConfig;
import me.lauriichan.spigot.justlootit.config.world.WorldConfig;
import me.lauriichan.spigot.justlootit.config.world.WorldMultiConfig;
import me.lauriichan.spigot.justlootit.data.CompatibilityContainer;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.storage.IStorage;
import me.lauriichan.spigot.justlootit.storage.Stored;
import me.lauriichan.spigot.justlootit.util.BlockUtil;

public class CustomStructuresListener implements Listener {

    private final CustomStructuresDataExtension dataExtension = CompatibilityDataExtension.get("CustomStructures",
        CustomStructuresDataExtension.class);

    private final String pluginId;

    private final VersionHandler versionHandler;
    private final ConfigManager configManager;
    
    private final MainConfig mainConfig;

    public CustomStructuresListener(final String pluginId, final VersionHandler versionHandler, final ConfigManager configManager) {
        this.pluginId = pluginId;
        this.versionHandler = versionHandler;
        this.configManager = configManager;
        this.mainConfig = configManager.config(MainConfig.class);
    }

    @EventHandler
    public void onLootPopulate(LootPopulateEvent event) {
        WorldConfig config = configManager.multiConfigOrCreate(WorldMultiConfig.class, event.getLocation().getWorld());
        if (mainConfig.worldWhitelistEnabled() && !config.isWhitelisted()) {
            return;
        }
        if (config.isCompatibilityContainerBlacklisted(pluginId)
            || config.isStructureBlacklisted(pluginId, event.getStructure().getName())) {
            return;
        }
        BlockState state = event.getLocation().getBlock().getState();
        if (!(state instanceof Container container) || JustLootItAccess.hasIdentity(container.getPersistentDataContainer())
            || JustLootItAccess.hasAnyOffset(container.getPersistentDataContainer())) {
            return;
        }
        Inventory inventory = container.getInventory();
        if (!JustLootItFlag.TILE_ENTITY_CONTAINERS.isSet()
            && JustLootItConstant.UNSUPPORTED_CONTAINER_TYPES.contains(inventory.getType())) {
            return;
        }
        event.setCanceled(true);
        BlockUtil.setContainerOffsetToNearbyChest(container);
        versionHandler.getLevel(state.getWorld()).getCapability(StorageCapability.class).ifPresent(capability -> {
            IStorage storage = capability.storage();
            Stored<?> stored;
            storage.write(stored = storage.registry().create(
                new CompatibilityContainer(dataExtension.create(event.getStructure().getName(), BlockUtil.getSeed(event.getLocation())))));
            JustLootItAccess.setIdentity(container.getPersistentDataContainer(), stored.id());
            container.update();
        });
    }

}
