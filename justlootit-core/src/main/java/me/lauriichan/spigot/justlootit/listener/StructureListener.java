package me.lauriichan.spigot.justlootit.listener;

import java.util.UUID;

import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.entity.ChestBoat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.AsyncStructureGenerateEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.Lootable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.BlockTransformer;
import org.bukkit.util.EntityTransformer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.lauriichan.minecraft.pluginbase.config.ConfigManager;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.listener.IListenerExtension;
import me.lauriichan.spigot.justlootit.JustLootItAccess;
import me.lauriichan.spigot.justlootit.JustLootItConstant;
import me.lauriichan.spigot.justlootit.JustLootItFlag;
import me.lauriichan.spigot.justlootit.JustLootItKey;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.config.world.WorldConfig;
import me.lauriichan.spigot.justlootit.config.world.WorldMultiConfig;
import me.lauriichan.spigot.justlootit.data.FrameContainer;
import me.lauriichan.spigot.justlootit.data.StaticContainer;
import me.lauriichan.spigot.justlootit.data.VanillaContainer;
import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.storage.IStorage;
import me.lauriichan.spigot.justlootit.storage.Stored;
import me.lauriichan.spigot.justlootit.util.BlockUtil;

@Extension
public class StructureListener implements IListenerExtension {

    private final Object2ObjectOpenHashMap<UUID, StructureTransformer> transformers = new Object2ObjectOpenHashMap<>();

    private final VersionHandler versionHandler;
    private final ConfigManager configManager;

    public StructureListener(final JustLootItPlugin plugin) {
        this.versionHandler = plugin.versionHandler();
        this.configManager = plugin.configManager();
    }

    @EventHandler
    public void onStructureGenerate(AsyncStructureGenerateEvent event) {
        WorldConfig config = configManager.multiConfigOrCreate(WorldMultiConfig.class, event.getWorld());
        if (config.isStructureBlacklisted(event.getStructure().getKey())) {
            return;
        }
        StructureTransformer transformer = transformers.get(event.getWorld().getUID());
        if (transformer == null || transformer.isTerminated()) {
            transformers.put(event.getWorld().getUID(), transformer = new StructureTransformer(versionHandler.getLevel(event.getWorld()), config));
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
        private final WorldConfig config;

        public StructureTransformer(final LevelAdapter level, final WorldConfig config) {
            this.level = level;
            this.config = config;
        }

        public boolean isTerminated() {
            return level.isTerminated();
        }

        @Override
        public BlockState transform(LimitedRegion region, int x, int y, int z, BlockState current, TransformationState state) {
            if (level.isTerminated()) {
                return current;
            }
            if (current instanceof Container container) {
                Inventory inventory = container.getInventory();
                if (!JustLootItFlag.TILE_ENTITY_CONTAINERS.isSet()
                    && JustLootItConstant.UNSUPPORTED_CONTAINER_TYPES.contains(inventory.getType())) {
                    return current;
                }
                PersistentDataContainer dataContainer = container.getPersistentDataContainer();
                if (JustLootItAccess.hasAnyOffset(dataContainer)) {
                    if (current instanceof Lootable lootable && lootable.getLootTable() != null) {
                        lootable.setLootTable(null);
                        lootable.setSeed(0);
                        container.update();
                    }
                    return current;
                } else if (current.getBlockData() instanceof Chest chest && chest.getType() != Type.SINGLE) {
                    Container otherContainer = BlockUtil.findChestAround(region, x, y, z, chest.getType(), chest.getFacing());
                    if (otherContainer != null) {
                        if (JustLootItAccess.hasIdentity(otherContainer.getPersistentDataContainer())) {
                            if (current instanceof Lootable lootable && lootable.getLootTable() != null) {
                                lootable.setLootTable(null);
                                lootable.setSeed(0);
                                container.update();
                            }
                            BlockUtil.setContainerOffset(container, otherContainer, true);
                            return current;
                        }
                    }
                }
                level.getCapability(StorageCapability.class).ifPresent(capability -> {
                    if (current instanceof Lootable lootable && lootable.getLootTable() != null) {
                        if (config.areVanillaContainersBlacklisted() || config.isLootTableBlacklisted(lootable.getLootTable().getKey())) {
                            return;
                        }
                        long id = getIdOfBlockState(region, x, y, z, capability.storage(), container);
                        Stored<?> stored = capability.storage().registry().create(new VanillaContainer(lootable.getLootTable(), lootable.getSeed())).id(id);
                        capability.storage().write(stored);
                        id = stored.id();
                        JustLootItAccess.setIdentity(dataContainer, id);
                        lootable.setSeed(0L);
                        lootable.setLootTable(null);
                        container.update();
                        return;
                    }
                    if (inventory.isEmpty()) {
                        Container otherContainer = BlockUtil.getNearbyChest(region, container);
                        if (otherContainer != null && JustLootItAccess.hasIdentity(otherContainer.getPersistentDataContainer())) {
                            BlockUtil.setContainerOffset(container, otherContainer, true);
                        }
                        return;
                    }
                    if (config.areStaticContainersBlacklisted()) {
                        return;
                    }
                    long id = getIdOfBlockState(region, x, y, z, capability.storage(), container);
                    Stored<?> stored = capability.storage().registry().create(new StaticContainer(inventory)).id(id);
                    capability.storage().write(stored);
                    id = stored.id();
                    JustLootItAccess.setIdentity(container.getPersistentDataContainer(), id);
                    JustLootItAccess.setIdentity(dataContainer, id);
                    inventory.clear();
                    container.update();
                });
            }
            return current;
        }

        private long getIdOfBlockState(LimitedRegion region, int x, int y, int z, IStorage storage, Container container) {
            BlockUtil.setContainerOffsetToNearbyChest(region, container);
            PersistentDataContainer dataContainer = container.getPersistentDataContainer();
            if (JustLootItAccess.hasIdentity(dataContainer)) {
                return JustLootItAccess.getIdentity(dataContainer);
            }
            return -1;
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
                        if (config.areFrameContainersBlacklisted()) {
                            return;
                        }
                        Stored<?> stored;
                        capability.storage().write(stored = capability.storage().registry().create(new FrameContainer(itemStack.clone())));
                        JustLootItAccess.setIdentity(itemFrame.getPersistentDataContainer(), stored.id());
                        itemFrame.setItem(null);
                    });
                }
            } else if (entity instanceof Minecart && entity instanceof Lootable lootable) {
                if (!JustLootItFlag.TILE_ENTITY_CONTAINERS.isSet() && entity.getType() == EntityType.MINECART_HOPPER) {
                    return allowedToSpawn;
                }
                if (lootable.getLootTable() != null) {
                    level.getCapability(StorageCapability.class).ifPresent(capability -> {
                        if (config.areVanillaContainersBlacklisted() || config.isLootTableBlacklisted(lootable.getLootTable().getKey())) {
                            return;
                        }
                        Stored<?> stored;
                        capability.storage().write(stored = capability.storage().registry().create(new VanillaContainer(lootable.getLootTable(), lootable.getSeed())));
                        JustLootItAccess.setIdentity(entity.getPersistentDataContainer(), stored.id());
                        lootable.setSeed(0L);
                        lootable.setLootTable(null);
                    });
                } else if (entity instanceof ChestBoat boat) {
                    Inventory inventory = boat.getInventory();
                    if (!inventory.isEmpty()) {
                        level.getCapability(StorageCapability.class).ifPresent(capability -> {
                            if (config.areStaticContainersBlacklisted()) {
                                return;
                            }
                            Stored<?> stored;
                            capability.storage().write(stored = capability.storage().registry().create(new StaticContainer(inventory)));
                            JustLootItAccess.setIdentity(entity.getPersistentDataContainer(), stored.id());
                            inventory.clear();
                        });
                    }
                }
            }
            return allowedToSpawn;
        }

    }

}
