package me.lauriichan.spigot.justlootit.compatibility.provider.betterstructures;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;

import com.magmaguy.betterstructures.api.WorldGenerationFinishEvent;
import com.magmaguy.betterstructures.modules.ModularWorld;

import me.lauriichan.minecraft.pluginbase.config.ConfigManager;
import me.lauriichan.spigot.justlootit.JustLootItAccess;
import me.lauriichan.spigot.justlootit.JustLootItConstant;
import me.lauriichan.spigot.justlootit.JustLootItFlag;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.config.world.WorldConfig;
import me.lauriichan.spigot.justlootit.config.world.WorldMultiConfig;
import me.lauriichan.spigot.justlootit.data.StaticContainer;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.storage.IStorage;
import me.lauriichan.spigot.justlootit.storage.Stored;
import me.lauriichan.spigot.justlootit.util.BlockUtil;

public class BetterStructuresModularListener implements Listener {

    private final String pluginId;

    private final VersionHandler versionHandler;
    private final ConfigManager configManager;

    public BetterStructuresModularListener(final String pluginId, final VersionHandler versionHandler, final ConfigManager configManager) {
        this.pluginId = pluginId;
        this.versionHandler = versionHandler;
        this.configManager = configManager;
    }

    @EventHandler
    public void onGenerationDone(WorldGenerationFinishEvent event) {
        ModularWorld modular = event.getModularWorld();
        World world = modular.getWorld();
        WorldConfig config = configManager.multiConfigOrCreate(WorldMultiConfig.class, world);
        if (config.isCompatibilityContainerBlacklisted(pluginId) || config.areStaticContainersBlacklisted()) {
            return;
        }
        for (Location location : modular.getChestLocations()) {
            convert(world, location);
        }
        for (Location location : modular.getBarrelLocations()) {
            convert(world, location);
        }
    }

    private void convert(World world, Location location) {
        if (!(world.getBlockState(location) instanceof Container container)) {
            return;
        }
        PersistentDataContainer dataContainer = container.getPersistentDataContainer();
        if (JustLootItAccess.hasIdentity(dataContainer) || JustLootItAccess.hasAnyOffset(dataContainer)) {
            return;
        }
        Inventory inventory = container.getInventory();
        if (!JustLootItFlag.TILE_ENTITY_CONTAINERS.isSet() && JustLootItConstant.UNSUPPORTED_CONTAINER_TYPES.contains(inventory.getType())
            || inventory.isEmpty()) {
            return;
        }
        Container otherContainer = BlockUtil.getNearbyChest(container);
        if (otherContainer != null) {
            BlockUtil.setContainerOffset(container, otherContainer, false);
        }
        versionHandler.getLevel(container.getWorld()).getCapability(StorageCapability.class).ifPresent(capability -> {
            IStorage storage = capability.storage();
            Stored<?> stored;
            storage.write(stored = storage.registry().create(new StaticContainer(inventory)));
            JustLootItAccess.setIdentity(dataContainer, stored.id());
            container.update();
            if (otherContainer != null) {
                otherContainer.update();
            }
        });
    }

}
