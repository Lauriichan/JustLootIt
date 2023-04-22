package me.lauriichan.spigot.justlootit.listener;

import java.util.UUID;

import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.ChestBoat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.AsyncStructureGenerateEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.event.world.AsyncStructureGenerateEvent.BlockTransformer;
import org.bukkit.event.world.AsyncStructureGenerateEvent.EntityTransformer;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.Lootable;
import org.bukkit.persistence.PersistentDataType;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.lauriichan.spigot.justlootit.JustLootItConstant;
import me.lauriichan.spigot.justlootit.JustLootItFlag;
import me.lauriichan.spigot.justlootit.JustLootItKey;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.data.FrameContainer;
import me.lauriichan.spigot.justlootit.data.StaticContainer;
import me.lauriichan.spigot.justlootit.data.VanillaContainer;
import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;

public class StructureListener implements Listener {

    private final Object2ObjectOpenHashMap<UUID, StructureTransformer> transformers = new Object2ObjectOpenHashMap<>();

    private final VersionHandler versionHandler;

    public StructureListener(final VersionHandler versionHandler) {
        this.versionHandler = versionHandler;
    }

    @EventHandler
    public void onStructureGenerate(AsyncStructureGenerateEvent event) {
        StructureTransformer transformer = transformers.get(event.getWorld().getUID());
        if (transformer == null || transformer.isTerminated()) {
            transformers.put(event.getWorld().getUID(), transformer = new StructureTransformer(versionHandler.getLevel(event.getWorld())));
        }
        event.setBlockTransformer(JustLootItKey.identity(), transformer);
        event.setEntityTransformer(JustLootItKey.identity(), transformer);
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        transformers.remove(event.getWorld().getUID());
    }

    private static final class StructureTransformer implements BlockTransformer, EntityTransformer {

        private final LevelAdapter level;

        public StructureTransformer(final LevelAdapter level) {
            this.level = level;
        }

        public boolean isTerminated() {
            return level.isTerminated();
        }

        @Override
        public BlockState transform(LimitedRegion region, int x, int y, int z, BlockState original, BlockState current, BlockState placed) {
            if (level.isTerminated()) {
                return current;
            }
            if (current instanceof Container container) {
                Inventory inventory = container.getInventory();
                if (inventory.isEmpty() || !JustLootItFlag.TILE_ENTITY_CONTAINERS.isSet() && JustLootItConstant.UNSUPPORTED_CONTAINER_TYPES.contains(inventory.getType())) {
                    return current;
                }
                level.getCapability(StorageCapability.class).ifPresent(capability -> {
                    if (current instanceof Lootable lootable && lootable.getLootTable() != null) {
                        long id;
                        if (container.getPersistentDataContainer().has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                            id = container.getPersistentDataContainer().get(JustLootItKey.identity(), PersistentDataType.LONG);
                        } else {
                            id = capability.storage().newId();
                            container.getPersistentDataContainer().set(JustLootItKey.identity(), PersistentDataType.LONG, id);
                        }
                        capability.storage().write(new VanillaContainer(id, lootable.getLootTable(), lootable.getSeed()));
                        lootable.setSeed(0L);
                        lootable.setLootTable(null);
                        container.update();
                        return;
                    }
                    long id;
                    if (container.getPersistentDataContainer().has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                        id = container.getPersistentDataContainer().get(JustLootItKey.identity(), PersistentDataType.LONG);
                    } else {
                        id = capability.storage().newId();
                        container.getPersistentDataContainer().set(JustLootItKey.identity(), PersistentDataType.LONG, id);
                    }
                    container.getPersistentDataContainer().set(JustLootItKey.identity(), PersistentDataType.LONG, id);
                    capability.storage().write(new StaticContainer(id, inventory));
                    inventory.clear();
                    container.update();
                });
            }
            return current;
        }

        @Override
        public boolean transform(LimitedRegion region, int x, int y, int z, Entity entity, boolean allowedToSpawn) {
            if (level.isTerminated()) {
                return allowedToSpawn;
            }
            if (entity instanceof ItemFrame itemFrame) {
                ItemStack itemStack = itemFrame.getItem();
                if (itemStack != null && !itemStack.getType().isAir()) {
                    level.getCapability(StorageCapability.class).ifPresent(capability -> {
                        long id = capability.storage().newId();
                        itemFrame.getPersistentDataContainer().set(JustLootItKey.identity(), PersistentDataType.LONG, id);
                        capability.storage().write(new FrameContainer(id, itemStack.clone()));
                        itemFrame.setItem(null);
                    });
                }
            } else if (entity instanceof Minecart && entity instanceof Lootable lootable) {
                if (!JustLootItFlag.TILE_ENTITY_CONTAINERS.isSet() && entity.getType() == EntityType.MINECART_HOPPER) {
                    return allowedToSpawn;
                }
                if (lootable.getLootTable() != null) {
                    level.getCapability(StorageCapability.class).ifPresent(capability -> {
                        long id = capability.storage().newId();
                        entity.getPersistentDataContainer().set(JustLootItKey.identity(), PersistentDataType.LONG, id);
                        capability.storage().write(new VanillaContainer(id, lootable.getLootTable(), lootable.getSeed()));
                        lootable.setSeed(0L);
                        lootable.setLootTable(null);
                    });
                } else if (entity instanceof ChestBoat boat) {
                    Inventory inventory = boat.getInventory();
                    if (!inventory.isEmpty()) {
                        level.getCapability(StorageCapability.class).ifPresent(capability -> {
                            long id = capability.storage().newId();
                            entity.getPersistentDataContainer().set(JustLootItKey.identity(), PersistentDataType.LONG, id);
                            capability.storage().write(new StaticContainer(id, inventory));
                            inventory.clear();
                        });
                    }
                }
            }
            return allowedToSpawn;
        }

    }

}
