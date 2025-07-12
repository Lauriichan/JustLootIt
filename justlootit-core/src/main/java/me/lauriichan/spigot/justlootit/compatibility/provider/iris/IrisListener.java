package me.lauriichan.spigot.justlootit.compatibility.provider.iris;

import java.util.Collections;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import com.volmit.iris.core.events.IrisLootEvent;
import com.volmit.iris.engine.data.cache.Cache;
import com.volmit.iris.engine.framework.Engine;
import com.volmit.iris.engine.object.InventorySlotType;
import com.volmit.iris.engine.object.IrisLootTable;
import com.volmit.iris.engine.object.IrisVanillaLootTable;
import com.volmit.iris.util.collection.KList;
import com.volmit.iris.util.math.BlockPosition;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.minecraft.pluginbase.config.ConfigManager;
import me.lauriichan.spigot.justlootit.JustLootItAccess;
import me.lauriichan.spigot.justlootit.JustLootItConstant;
import me.lauriichan.spigot.justlootit.JustLootItFlag;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.compatibility.data.CompatibilityDataExtension;
import me.lauriichan.spigot.justlootit.compatibility.data.iris.IIrisTableKey;
import me.lauriichan.spigot.justlootit.compatibility.data.iris.IrisDataExtension;
import me.lauriichan.spigot.justlootit.compatibility.data.iris.IIrisTableKey.IrisTableKey;
import me.lauriichan.spigot.justlootit.compatibility.data.iris.IIrisTableKey.VanillaTableKey;
import me.lauriichan.spigot.justlootit.config.world.WorldConfig;
import me.lauriichan.spigot.justlootit.config.world.WorldMultiConfig;
import me.lauriichan.spigot.justlootit.data.CompatibilityContainer;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.storage.IStorage;
import me.lauriichan.spigot.justlootit.storage.Stored;
import me.lauriichan.spigot.justlootit.util.BlockUtil;

public class IrisListener implements Listener {

    private final IrisDataExtension dataExtension = CompatibilityDataExtension.get("Iris", IrisDataExtension.class);

    private final String pluginId;

    private final VersionHandler versionHandler;
    private final ConfigManager configManager;

    public IrisListener(final String pluginId, final VersionHandler versionHandler, final ConfigManager configManager) {
        this.pluginId = pluginId;
        this.versionHandler = versionHandler;
        this.configManager = configManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLoot(IrisLootEvent event) {
        if (event.getEngine().isStudio() || event.getSlot() != InventorySlotType.STORAGE || event.getTables().isEmpty()) {
            return;
        }
        BlockState state = event.getBlock().getState();
        if (!(state instanceof Container container) || JustLootItAccess.hasIdentity(container.getPersistentDataContainer())
            || JustLootItAccess.hasAnyOffset(container.getPersistentDataContainer())) {
            return;
        }
        Inventory inventory = container.getInventory();
        if (!JustLootItFlag.TILE_ENTITY_CONTAINERS.isSet()
            && JustLootItConstant.UNSUPPORTED_CONTAINER_TYPES.contains(inventory.getType())) {
            return;
        }
        Location loc = event.getBlock().getLocation();
        WorldConfig config = configManager.multiConfigOrCreate(WorldMultiConfig.class, event.getBlock().getWorld());
        if (config.isCompatibilityContainerBlacklisted(pluginId)) {
            return;
        }
        int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
        String structureId = getStructureId(event.getEngine(), x, y, z);
        if (structureId != null && config.isStructureBlacklisted(pluginId, structureId)) {
            return;
        }
        IIrisTableKey[] keys = create(config, event.getTables());
        event.getTables().clear();
        BlockUtil.setContainerOffsetToNearbyChest(container);
        long seed = Cache.key(x >> 4, z >> 4) + BlockPosition.toLong(x, loc.getBlockY(), z);
        versionHandler.getLevel(event.getBlock().getWorld()).getCapability(StorageCapability.class).ifPresent(capability -> {
            IStorage storage = capability.storage();
            Stored<?> stored;
            storage.write(stored = storage.registry().create(new CompatibilityContainer(dataExtension.create(keys, seed))));
            JustLootItAccess.setIdentity(container.getPersistentDataContainer(), stored.id());
            container.update();
        });
    }
    
    private IIrisTableKey[] create(WorldConfig config, KList<IrisLootTable> lootTables) {
        ObjectArrayList<IIrisTableKey> keys = new ObjectArrayList<>(lootTables.size());
        for (IrisLootTable table : lootTables) {
            if (table instanceof IrisVanillaLootTable vanilla) {
                if (config.isLootTableBlacklisted(vanilla.getLootTable().getKey())) {
                    continue;
                }
                keys.add(new VanillaTableKey(vanilla.getLootTable().getKey()));
                continue;
            }
            if (config.isLootTableBlacklisted(pluginId, table.getLoadKey())) {
                continue;
            }
            keys.add(new IrisTableKey(table.getLoadKey()));

        }
        Collections.sort(keys);
        return keys.toArray(IIrisTableKey[]::new);
    }

    private String getStructureId(Engine engine, int x, int y, int z) {
        String object = engine.getMantle().getMantle().get(x, y - engine.getMinHeight(), z, String.class);
        if (object == null) {
            return null;
        }
        return object.split("\\Q@\\E")[0];
    }

}
