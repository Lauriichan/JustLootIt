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
import me.lauriichan.spigot.justlootit.config.world.WorldConfig;
import me.lauriichan.spigot.justlootit.config.world.WorldMultiConfig;
import me.lauriichan.spigot.justlootit.data.CompatibilityContainer;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.storage.IStorage;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.util.BlockUtil;

public class CustomStructuresListener implements Listener {
    
    private final CustomStructuresDataExtension dataExtension = CompatibilityDataExtension.get("CustomStructures",
        CustomStructuresDataExtension.class);
    
    private final String pluginId;

    private final VersionHandler versionHandler;
    private final ConfigManager configManager;

    public CustomStructuresListener(final String pluginId, final VersionHandler versionHandler, final ConfigManager configManager) {
        this.pluginId = pluginId;
        this.versionHandler = versionHandler;
        this.configManager = configManager;
    }

    @EventHandler
    public void onLootPopulate(LootPopulateEvent event) {
        BlockState state = event.getLocation().getBlock().getState();
        if (!(state instanceof Container container) || JustLootItAccess.hasIdentity(container.getPersistentDataContainer()) || JustLootItAccess.hasAnyOffset(container.getPersistentDataContainer())) {
            return;
        }
        Inventory inventory = container.getInventory();
        if (!JustLootItFlag.TILE_ENTITY_CONTAINERS.isSet()
            && JustLootItConstant.UNSUPPORTED_CONTAINER_TYPES.contains(inventory.getType())) {
            return;
        }
        WorldConfig config = configManager.multiConfigOrCreate(WorldMultiConfig.class, event.getLocation().getWorld());
        if (config.isCompatibilityContainerBlacklisted(pluginId)
            || config.isStructureBlacklisted(pluginId, event.getStructure().getName())) {
            return;
        }
        event.setCanceled(true);
        BlockUtil.setContainerOffsetToNearbyChest(container);
        versionHandler.getLevel(state.getWorld()).getCapability(StorageCapability.class).ifPresent(capability -> {
            IStorage<Storable> storage = capability.storage();
            long id = storage.newId();
            storage.write(new CompatibilityContainer(id,
                dataExtension.create(event.getStructure().getName(), BlockUtil.getSeed(event.getLocation()))));
            JustLootItAccess.setIdentity(container.getPersistentDataContainer(), id);
            container.update();
        });
    }

}
