package me.lauriichan.spigot.justlootit.convert;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.spigot.justlootit.JustLootItConstant;
import me.lauriichan.spigot.justlootit.JustLootItFlag;
import me.lauriichan.spigot.justlootit.JustLootItKey;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.data.FrameContainer;
import me.lauriichan.spigot.justlootit.data.StaticContainer;
import me.lauriichan.spigot.justlootit.data.VanillaContainer;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoChunk;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoEntity;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoWorld;
import me.lauriichan.spigot.justlootit.nms.nbt.ICompoundTag;
import me.lauriichan.spigot.justlootit.nms.nbt.IListTag;
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;
import me.lauriichan.spigot.justlootit.storage.IStorage;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.util.BlockUtil;
import me.lauriichan.spigot.justlootit.util.EntityUtil;
import me.lauriichan.spigot.justlootit.util.SimpleDataType;

public class VanillaConverter extends ChunkConverter {

    public VanillaConverter(VersionHandler versionHandler, ConversionProperties properties) {
        super(versionHandler, properties);
    }

    // TODO: FIX THIS MESS CAUSE WE CAN'T USE org.bukkit.block.BlockState!!!

    @Override
    public void convert(ProtoChunk chunk, Random random) {
        IStorage<Storable> storage = chunk.getWorld().getCapability(StorageCapability.class).map(StorageCapability::storage).get();
        if (!chunk.getBlockEntities().isEmpty()) {
            ObjectArrayList<BlockState> pendingBlockEntities = new ObjectArrayList<>(chunk.getBlockEntities());
            while (!pendingBlockEntities.isEmpty()) {
                BlockState state = pendingBlockEntities.remove(0);
                if (!(state instanceof Container container)) {
                    return;
                }
                Inventory inventory = container.getInventory();
                if (!JustLootItFlag.TILE_ENTITY_CONTAINERS.isSet()
                    && JustLootItConstant.UNSUPPORTED_CONTAINER_TYPES.contains(inventory.getType())) {
                    return;
                }
                PersistentDataContainer dataContainer = container.getPersistentDataContainer();
                if (dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                    continue;
                }
                ItemStack[] otherItems = null;
                boolean otherItemsIsLeft = false;
                LootTable lootTable = null;
                long seed = 0;
                if (state instanceof Lootable lootable && lootable.getLootTable() != null) {
                    lootTable = lootable.getLootTable();
                    seed = lootable.getSeed();
                }
                if (state.getBlockData() instanceof Chest chest && chest.getType() != Type.SINGLE) {
                    if (dataContainer.has(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR)) {
                        continue;
                    }
                    Location otherLocation = BlockUtil.findChestLocationAround(state.getLocation(), chest.getType(), chest.getFacing());
                    Location location = state.getLocation();
                    BlockState otherState = pendingBlockEntities.stream().filter(pending -> pending.getLocation().equals(otherLocation)).findFirst().orElse(null);
                    otherItemsIsLeft = chest.getType() == Type.RIGHT;
                    if (otherState == null) {
                        // We convert double chests to single chests in this process
                        // The reason why is that we can't convert chests that are across borders
                        // Therefore we don't know how to convert this
                        chest.setType(Chest.Type.SINGLE);
                        state.setBlockData(chest);
                    } else if (otherState instanceof Container otherContainer && otherState.getBlockData() instanceof Chest) {
                        pendingBlockEntities.remove(otherState);
                        PersistentDataContainer otherDataContainer = otherContainer.getPersistentDataContainer();
                        otherDataContainer.set(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR, otherLocation.toVector().subtract(location.toVector()));
                        dataContainer.set(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR, location.toVector().subtract(otherLocation.toVector()));
                        otherState.update();
                        chunk.updateBlockEntity(otherState);
                        if (lootTable == null) {
                            if (otherContainer instanceof Lootable lootable) {
                                lootTable = lootable.getLootTable();
                                seed = lootable.getSeed();
                                if (lootTable == null) {
                                    if (!properties.isProperty(ConvProp.VANILLA_ALLOW_STATIC_CONTAINER)) {
                                        continue;
                                    }
                                    otherItems = otherContainer.getInventory().getContents();
                                }
                            } else {
                                otherItems = otherContainer.getInventory().getContents();
                            }
                        }
                    }
                } else if (lootTable == null && !properties.isProperty(ConvProp.VANILLA_ALLOW_STATIC_CONTAINER)) {
                    continue;
                }
                if (lootTable != null) {
                    long storageId = storage.newId();
                    storage.write(new VanillaContainer(storageId, lootTable, seed == 0 ? random.nextLong() : seed));
                    dataContainer.set(JustLootItKey.identity(), PersistentDataType.LONG, storageId);
                } else {
                    ItemStack[] items = container.getInventory().getContents();
                    if (otherItems != null) {
                        ItemStack[] tmpItems = new ItemStack[items.length + otherItems.length];
                        if (otherItemsIsLeft) {
                            System.arraycopy(otherItems, 0, tmpItems, 0, otherItems.length);
                            System.arraycopy(items, 0, tmpItems, otherItems.length, items.length);
                        } else {
                            System.arraycopy(items, 0, tmpItems, 0, items.length);
                            System.arraycopy(otherItems, 0, tmpItems, items.length, otherItems.length);
                        }
                        items = tmpItems;
                    }
                    long storageId = storage.newId();
                    storage.write(new StaticContainer(storageId, items));
                    dataContainer.set(JustLootItKey.identity(), PersistentDataType.LONG, storageId);
                }
            }
        }
        for (ProtoEntity entity : chunk.getEntities()) {
            PersistentDataContainer dataContainer = entity.getContainer();
            if (dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                continue;
            }
            ICompoundTag tag = entity.getNbt();
            EntityType type = entity.getType();
            if (EntityUtil.isItemFrame(type)) {
                if (!properties.isProperty(ConvProp.VANILLA_ALLOW_STATIC_CONTAINER) || !tag.has("Item", TagType.COMPOUND)) {
                    continue;
                }
                ItemStack item = nbtHelper.asItem(tag.getCompound("Item"));
                if (item.getType().isAir()
                    || (properties.isProperty(ConvProp.VANILLA_ALLOW_ONLY_ELYTRA_FRAME) && item.getType() != Material.ELYTRA)) {
                    continue;
                }
                long storageId = storage.newId();
                storage.write(new FrameContainer(storageId, item));
                dataContainer.set(JustLootItKey.identity(), PersistentDataType.LONG, storageId);
                continue;
            }
            if (EntityUtil.isSuppportedEntity(type)) {
                if (tag.has("LootTable", TagType.STRING)) {
                    long storageId = storage.newId();
                    storage.write(new VanillaContainer(storageId, NamespacedKey.fromString(tag.getString("LootTable")),
                        tag.hasNumeric("LootTableSeed") ? tag.getLong("LootTableSeed") : random.nextLong()));
                    dataContainer.set(JustLootItKey.identity(), PersistentDataType.LONG, storageId);
                    tag.remove("LootTable");
                    tag.remove("LootTableSeed");
                    continue;
                } else if (!properties.isProperty(ConvProp.VANILLA_ALLOW_STATIC_CONTAINER) || !tag.hasList("Items", TagType.COMPOUND)) {
                    continue;
                }
                IListTag<ICompoundTag> itemListTag = tag.getList("Items", TagType.COMPOUND);
                if (itemListTag.isEmpty()) {
                    continue;
                }
                ItemStack[] items = new ItemStack[EntityUtil.getInventorySize(type)];
                boolean emptyInventory = true;
                for (ICompoundTag itemTag : itemListTag) {
                    int slot = itemTag.getInt("Slot");
                    if (slot >= items.length || slot < 0) {
                        continue;
                    }
                    ItemStack item = nbtHelper.asItem(itemTag);
                    if (item == null || item.getType().isAir()) {
                        continue;
                    }
                    items[slot] = item;
                    emptyInventory = false;
                }
                if (emptyInventory) {
                    continue;
                }
                tag.remove("Items");
                long storageId = storage.newId();
                storage.write(new StaticContainer(storageId, items));
                dataContainer.set(JustLootItKey.identity(), PersistentDataType.LONG, storageId);
                continue;
            }
        }
    }

    @Override
    boolean isEnabled() {
        return properties.isProperty(ConvProp.DO_VANILLA_CONVERSION);
    }

    @Override
    boolean isEnabledFor(ProtoWorld world) {
        return world.hasCapability(StorageCapability.class);
    }

}
