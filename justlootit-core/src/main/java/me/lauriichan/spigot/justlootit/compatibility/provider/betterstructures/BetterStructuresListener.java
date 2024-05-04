package me.lauriichan.spigot.justlootit.compatibility.provider.betterstructures;

import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.block.data.type.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.magmaguy.betterstructures.api.BuildPlaceEvent;
import com.magmaguy.betterstructures.api.ChestFillEvent;
import com.magmaguy.betterstructures.buildingfitter.FitAnything;
import com.magmaguy.betterstructures.buildingfitter.util.LocationProjector;
import com.magmaguy.betterstructures.schematics.SchematicContainer;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import me.lauriichan.spigot.justlootit.JustLootItConstant;
import me.lauriichan.spigot.justlootit.JustLootItFlag;
import me.lauriichan.spigot.justlootit.JustLootItKey;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.compatibility.data.CompatibilityDataExtension;
import me.lauriichan.spigot.justlootit.compatibility.data.betterstructures.BetterStructuresDataExtension;
import me.lauriichan.spigot.justlootit.data.CompatibilityContainer;
import me.lauriichan.spigot.justlootit.data.StaticContainer;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.util.Vec3i;
import me.lauriichan.spigot.justlootit.storage.IStorage;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.util.BlockUtil;
import me.lauriichan.spigot.justlootit.util.SimpleDataType;

public class BetterStructuresListener implements Listener {

    private final VersionHandler versionHandler;
    private final BetterStructuresDataExtension dataExtension = CompatibilityDataExtension.get("BetterStructures",
        BetterStructuresDataExtension.class);

    private final Object2ObjectMap<Location, String> lootFileNameMap = Object2ObjectMaps.synchronize(new Object2ObjectArrayMap<>());

    public BetterStructuresListener(final VersionHandler versionHandler) {
        this.versionHandler = versionHandler;
    }

    @EventHandler
    public void onChestFill(ChestFillEvent event) {
        Container container = event.getContainer();
        Location location = container.getLocation();
        PersistentDataContainer dataContainer = container.getPersistentDataContainer();
        if (dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)
            || dataContainer.has(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR)) {
            lootFileNameMap.remove(location);
            return;
        }
        Inventory inventory = container.getInventory();
        if (!JustLootItFlag.TILE_ENTITY_CONTAINERS.isSet()
            && JustLootItConstant.UNSUPPORTED_CONTAINER_TYPES.contains(inventory.getType())) {
            lootFileNameMap.remove(location);
            return;
        }
        String fileName = lootFileNameMap.remove(location);
        if (container.getBlockData() instanceof Chest chest && chest.getType() != Chest.Type.SINGLE) {
            Container otherChest = BlockUtil.findChestAround(container.getWorld(), container.getLocation(), chest.getType(),
                chest.getFacing());
            if (otherChest.getBlockData() instanceof Chest) {
                otherChest.getPersistentDataContainer().set(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR,
                    new Vec3i(container.getLocation()).subtractOf(otherChest.getLocation()));
                container.getPersistentDataContainer().set(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR,
                    new Vec3i(otherChest.getLocation()).subtractOf(container.getLocation()));
                otherChest.update();
                if (fileName == null) {
                    fileName = lootFileNameMap.remove(otherChest.getLocation());
                }
            }
        }
        String fFileName = fileName;
        versionHandler.getLevel(container.getWorld()).getCapability(StorageCapability.class).ifPresent(capability -> {
            if (fFileName == null) {
                if (inventory.isEmpty()) {
                    return;
                }
                event.setCancelled(true);
                IStorage<Storable> storage = capability.storage();
                long id = storage.newId();
                storage.write(new StaticContainer(id, inventory));
                dataContainer.set(JustLootItKey.identity(), PersistentDataType.LONG, id);
                container.update();
                return;
            }
            event.setCancelled(true);
            IStorage<Storable> storage = capability.storage();
            long id = storage.newId();
            storage.write(new CompatibilityContainer(id, dataExtension.create(fFileName)));
            dataContainer.set(JustLootItKey.identity(), PersistentDataType.LONG, id);
            container.update();
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BuildPlaceEvent event) {
        FitAnything fit = event.getFitAnything();
        SchematicContainer schematic = fit.getSchematicContainer();
        if (schematic.getGeneratorConfigFields().getChestContents() == null) {
            return;
        }
        schematic.getChestLocations().stream().map(vector -> LocationProjector.project(fit.getLocation(), fit.getSchematicOffset(), vector))
            .forEach(location -> {
                lootFileNameMap.put(location, schematic.getGeneratorConfigFields().getFilename());
            });
    }

}
