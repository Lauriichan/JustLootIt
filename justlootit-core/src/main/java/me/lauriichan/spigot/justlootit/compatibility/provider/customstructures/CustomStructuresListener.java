package me.lauriichan.spigot.justlootit.compatibility.provider.customstructures;

import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.type.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataType;

import com.ryandw11.structure.api.LootPopulateEvent;

import me.lauriichan.spigot.justlootit.JustLootItConstant;
import me.lauriichan.spigot.justlootit.JustLootItFlag;
import me.lauriichan.spigot.justlootit.JustLootItKey;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.compatibility.data.CompatibilityDataExtension;
import me.lauriichan.spigot.justlootit.compatibility.data.customstructures.CustomStructuresDataExtension;
import me.lauriichan.spigot.justlootit.data.CompatibilityContainer;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.util.Vec3i;
import me.lauriichan.spigot.justlootit.storage.IStorage;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.util.BlockUtil;
import me.lauriichan.spigot.justlootit.util.SimpleDataType;

public class CustomStructuresListener implements Listener {

    private final VersionHandler versionHandler;
    private final CustomStructuresDataExtension dataExtension = CompatibilityDataExtension.get("CustomStructures",
        CustomStructuresDataExtension.class);

    public CustomStructuresListener(final VersionHandler versionHandler) {
        this.versionHandler = versionHandler;
    }

    @EventHandler
    public void onLootPopulate(LootPopulateEvent event) {
        BlockState state = event.getLocation().getBlock().getState();
        if (!(state instanceof Container container)
            || container.getPersistentDataContainer().has(JustLootItKey.identity(), PersistentDataType.LONG)
            || container.getPersistentDataContainer().has(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR)) {
            return;
        }
        Inventory inventory = container.getInventory();
        if (!JustLootItFlag.TILE_ENTITY_CONTAINERS.isSet()
            && JustLootItConstant.UNSUPPORTED_CONTAINER_TYPES.contains(inventory.getType())) {
            return;
        }
        if (state.getBlockData() instanceof Chest chest && chest.getType() != Chest.Type.SINGLE) {
            Container otherChest = BlockUtil.findChestAround(state.getWorld(), event.getLocation(), chest.getType(), chest.getFacing());
            if (otherChest.getBlockData() instanceof Chest) {
                otherChest.getPersistentDataContainer().set(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR,
                    new Vec3i(container.getLocation()).subtractOf(otherChest.getLocation()));
                container.getPersistentDataContainer().set(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR,
                    new Vec3i(otherChest.getLocation()).subtractOf(container.getLocation()));
                otherChest.update();
            }
        }
        versionHandler.getLevel(state.getWorld()).getCapability(StorageCapability.class).ifPresent(capability -> {
            IStorage<Storable> storage = capability.storage();
            long id = storage.newId();
            storage.write(new CompatibilityContainer(id,
                dataExtension.create(event.getStructure().getName(), BlockUtil.getSeed(event.getLocation()))));
            container.getPersistentDataContainer().set(JustLootItKey.identity(), PersistentDataType.LONG, id);
            container.update();
        });
    }

}
