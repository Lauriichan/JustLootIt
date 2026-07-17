package me.lauriichan.spigot.justlootit.compatibility.provider.betterstructures;

import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;

import com.magmaguy.betterstructures.api.BuildPlaceEvent;
import com.magmaguy.betterstructures.api.ChestFillEvent;
import com.magmaguy.betterstructures.buildingfitter.FitAnything;
import com.magmaguy.betterstructures.buildingfitter.util.LocationProjector;
import com.magmaguy.betterstructures.schematics.SchematicContainer;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import me.lauriichan.minecraft.pluginbase.config.ConfigManager;
import me.lauriichan.spigot.justlootit.JustLootItAccess;
import me.lauriichan.spigot.justlootit.JustLootItConstant;
import me.lauriichan.spigot.justlootit.JustLootItFlag;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.compatibility.data.CompatibilityDataExtension;
import me.lauriichan.spigot.justlootit.compatibility.data.betterstructures.BetterStructuresDataExtension;
import me.lauriichan.spigot.justlootit.config.MainConfig;
import me.lauriichan.spigot.justlootit.config.world.WorldConfig;
import me.lauriichan.spigot.justlootit.config.world.WorldMultiConfig;
import me.lauriichan.spigot.justlootit.data.CompatibilityContainer;
import me.lauriichan.spigot.justlootit.data.StaticContainer;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.storage.IStorage;
import me.lauriichan.spigot.justlootit.storage.Stored;
import me.lauriichan.spigot.justlootit.util.BlockUtil;

public class BetterStructuresListenerV2 implements Listener {

    private final BetterStructuresDataExtension dataExtension = CompatibilityDataExtension.get("BetterStructures",
        BetterStructuresDataExtension.class);

    private final ObjectSet<Location> blacklistedLocation = ObjectSets.synchronize(new ObjectArraySet<>());

    private final String pluginId;

    private final VersionHandler versionHandler;
    private final ConfigManager configManager;

    private final MainConfig mainConfig;

    public BetterStructuresListenerV2(final String pluginId, final VersionHandler versionHandler, final ConfigManager configManager) {
        this.pluginId = pluginId;
        this.versionHandler = versionHandler;
        this.configManager = configManager;
        this.mainConfig = configManager.config(MainConfig.class);
    }

    @EventHandler
    public void onChestFill(ChestFillEvent event) {
        Container container = event.getContainer();
        WorldConfig config = configManager.multiConfigOrCreate(WorldMultiConfig.class, container.getWorld());
        if (mainConfig.worldWhitelistEnabled() && !config.isWhitelisted()) {
            return;
        }
        if (config.isCompatibilityContainerBlacklisted(pluginId) || blacklistedLocation.contains(container.getLocation())) {
            return;
        }
        String treasureFile = event.getTreasureConfigFilename();
        if (config.isLootTableBlacklisted("betterstructures", treasureFile)) {
            return;
        }
        PersistentDataContainer dataContainer = container.getPersistentDataContainer();
        if (JustLootItAccess.hasIdentity(dataContainer) || JustLootItAccess.hasAnyOffset(dataContainer)) {
            ;
            return;
        }
        Inventory inventory = container.getSnapshotInventory();
        if (!JustLootItFlag.TILE_ENTITY_CONTAINERS.isSet()
            && JustLootItConstant.UNSUPPORTED_CONTAINER_TYPES.contains(inventory.getType())) {
            return;
        }
        String fileName = event.getTreasureConfigFilename();
        Container otherContainer = BlockUtil.getNearbyChest(container);
        if (otherContainer != null) {
            BlockUtil.setContainerOffset(container, otherContainer, false);
        }
        String fFileName = fileName;
        versionHandler.getLevel(container.getWorld()).getCapability(StorageCapability.class).ifPresent(capability -> {
            if (fFileName == null) {
                if (inventory.isEmpty() || config.areStaticContainersBlacklisted()) {
                    return;
                }
                event.setCancelled(true);
                IStorage storage = capability.storage();
                Stored<?> stored;
                storage.write(stored = storage.registry().create(new StaticContainer(inventory)));
                JustLootItAccess.setIdentity(dataContainer, stored.id());
                container.update();
                if (otherContainer != null) {
                    otherContainer.update();
                }
                return;
            }
            if (config.isLootTableBlacklisted(pluginId, fFileName)) {
                return;
            }
            event.setCancelled(true);
            IStorage storage = capability.storage();
            Stored<?> stored;
            storage.write(stored = storage.registry().create(new CompatibilityContainer(dataExtension.create(fFileName))));
            JustLootItAccess.setIdentity(dataContainer, stored.id());
            container.update();
            if (otherContainer != null) {
                otherContainer.update();
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSchematicPlace(BuildPlaceEvent event) {
        FitAnything fit = event.getFitAnything();
        WorldConfig config = configManager.multiConfigOrCreate(WorldMultiConfig.class, fit.getLocation().getWorld());
        if (mainConfig.worldWhitelistEnabled() && !config.isWhitelisted()) {
            return;
        }
        if (config.isCompatibilityContainerBlacklisted(pluginId)) {
            return;
        }
        SchematicContainer schematic = fit.getSchematicContainer();
        if (!config.isStructureBlacklisted(pluginId, schematic.getConfigFilename())) {
            return;
        }
        schematic.getChestLocations().stream().map(vector -> LocationProjector.project(fit.getLocation(), fit.getSchematicOffset(), vector))
            .forEach(blacklistedLocation::add);
    }

}
