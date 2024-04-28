package me.lauriichan.spigot.justlootit.convert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.Lootable;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
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
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;
import me.lauriichan.spigot.justlootit.storage.IStorage;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.util.BlockUtil;
import me.lauriichan.spigot.justlootit.util.EntityUtil;
import me.lauriichan.spigot.justlootit.util.SimpleDataType;

public class LootinConverter extends ChunkConverter {

    public static final SimpleDataType<byte[], ItemStack[]> ITEM_STACK_ARRAY_TYPE = new SimpleDataType<>(byte[].class, ItemStack[].class) {
        @Override
        public byte[] toPrimitive(ItemStack[] complex, PersistentDataAdapterContext context) {
            try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                final BukkitObjectOutputStream bukkitOutputStream = new BukkitObjectOutputStream(outputStream)) {
                bukkitOutputStream.writeInt(complex.length);
                for (ItemStack itemStack : complex) {
                    bukkitOutputStream.writeObject(itemStack);
                }
                return outputStream.toByteArray();
            } catch (IOException e) {
                throw new IllegalStateException("Failed to serialize ItemStack[] to byte[]", e);
            }
        }

        @Override
        public ItemStack[] fromPrimitive(byte[] primitive, PersistentDataAdapterContext context) {
            try (final ByteArrayInputStream inputStream = new ByteArrayInputStream(primitive);
                final BukkitObjectInputStream bukkitInputStream = new BukkitObjectInputStream(inputStream)) {
                ItemStack[] array = new ItemStack[bukkitInputStream.readInt()];
                for (int i = 0; i < array.length; i++) {
                    array[i] = (ItemStack) bukkitInputStream.readObject();
                }
                return array;
            } catch (EOFException e) {
                return new ItemStack[0];
            } catch (IOException | ClassNotFoundException e) {
                throw new IllegalStateException("Failed to deserialize byte[] to ItemStack[]", e);
            }
        }
    };

    private final NamespacedKey identityKey = NamespacedKey.fromString("lootin:Lootin");
    private final NamespacedKey elytraKey = NamespacedKey.fromString("lootin:item-frame-elytra-key");

    private final NamespacedKey dataKey = NamespacedKey.fromString("lootin:loot-container");
    private final NamespacedKey loottableKey = NamespacedKey.fromString("lootin:lottable");

    private final NamespacedKey rwgLoottableKey = NamespacedKey.fromString("lootin:rwg-loottable-key");
    private final NamespacedKey rwgIdentityKey = NamespacedKey.fromString("lootin:rwg-identity-key");
    private final NamespacedKey betterStructuresKey = NamespacedKey.fromString("lootin:better-structures-name-key");
    private final NamespacedKey customStructuresKey = NamespacedKey.fromString("lootin:custom-structures-name-key");

    private final ItemStack airPlaceholderItem = ItemEditor.of(Material.STICK)
        .applyItemMeta(meta -> meta.setDisplayName("This is temporary Item")).asItemStack();
    
    public LootinConverter(VersionHandler versionHandler, ConversionProperties properties) {
        super(versionHandler, properties);
    }

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
                if (!JustLootItFlag.TILE_ENTITY_CONTAINERS.isSet()
                    && JustLootItConstant.UNSUPPORTED_CONTAINER_TYPES.contains(container.getInventory().getType())) {
                    return;
                }
                PersistentDataContainer dataContainer = container.getPersistentDataContainer();
                if (!dataContainer.has(identityKey, PersistentDataType.STRING) || hasUnsupportedKey(dataContainer)) {
                    return;
                }
                boolean loottable = dataContainer.has(loottableKey, PersistentDataType.STRING);
                ItemStack[] otherItems = null;
                boolean otherItemsIsLeft = false;
                if (state.getBlockData() instanceof Chest chest && chest.getType() != Chest.Type.SINGLE) {
                    Location otherLocation = BlockUtil.findChestLocationAround(state.getLocation(), chest.getType(), chest.getFacing());
                    Location location = state.getLocation();
                    BlockState otherState = pendingBlockEntities.stream().filter(pending -> pending.getLocation().equals(otherLocation)).findFirst().orElse(null);
                    if (otherState == null) {
                        // We convert double chests to single chests in this process
                        // The reason why is that we can't convert chests that are across borders
                        // Therefore we don't know how to convert this
                        chest.setType(Chest.Type.SINGLE);
                        state.setBlockData(chest);
                    } else if (otherState instanceof Container otherContainer && otherState.getBlockData() instanceof Chest) {
                        pendingBlockEntities.remove(otherState);
                        PersistentDataContainer otherDataContainer = otherContainer.getPersistentDataContainer();
                        otherDataContainer.remove(identityKey);
                        if (!loottable) {
                            otherItems = extractContents(otherDataContainer);
                            otherItemsIsLeft = chest.getType() == Type.RIGHT;
                        }
                        clearLootinKeys(otherDataContainer);
                        otherDataContainer.set(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR, otherLocation.toVector().subtract(location.toVector()));
                        dataContainer.set(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR, location.toVector().subtract(otherLocation.toVector()));
                        otherState.update();
                        chunk.updateBlockEntity(otherState);
                    }
                }
                try {
                    if (loottable) {
                        long seed = 0;
                        if (state instanceof Lootable lootable) {
                            seed = lootable.getSeed();
                        }
                        long storageId = storage.newId();
                        storage.write(new VanillaContainer(storageId, NamespacedKey.fromString(dataContainer.get(loottableKey, PersistentDataType.STRING)), seed == 0 ? random.nextLong() : seed));
                        dataContainer.set(JustLootItKey.identity(), PersistentDataType.LONG, storageId);
                    } else {
                        ItemStack[] items = extractContents(dataContainer);
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
                        for (int i = 0; i < items.length; i++) {
                            ItemStack item = items[i];
                            if (items[i] == null || !item.getType().isAir()) {
                                continue;
                            }
                            items[i] = null;
                        }
                        long storageId = storage.newId();
                        storage.write(new StaticContainer(storageId, items));
                        dataContainer.set(JustLootItKey.identity(), PersistentDataType.LONG, storageId);
                    }
                } finally {
                    clearLootinKeys(dataContainer);
                    container.update();
                    chunk.updateBlockEntity(state);
                }
            }
        }
        for (ProtoEntity entity : chunk.getEntities()) {
            if (EntityUtil.isItemFrame(entity.getType())) {
                PersistentDataContainer dataContainer = entity.getContainer();
                if (!dataContainer.has(elytraKey, PersistentDataType.INTEGER)) {
                    return;
                }
                try {
                    long storageId = storage.newId();
                    // we know it's an elytra cause Lootin doesn't support any other items
                    storage.write(new FrameContainer(storageId, new ItemStack(Material.ELYTRA)));
                    dataContainer.set(JustLootItKey.identity(), PersistentDataType.LONG, storageId);
                } finally {
                    clearLootinKeys(dataContainer);
                    chunk.updateEntity(entity);
                }
            } else if (EntityUtil.isSuppportedEntity(entity.getType())) {
                PersistentDataContainer dataContainer = entity.getContainer();
                if (!dataContainer.has(identityKey, PersistentDataType.STRING) || hasUnsupportedKey(dataContainer)) {
                    return;
                }
                try {
                    if (dataContainer.has(loottableKey, PersistentDataType.STRING)) {
                        long seed;
                        if (entity.getNbt().has("LootTableSeed", TagType.LONG)) {
                            seed = entity.getNbt().getLong("LootTableSeed");
                        } else {
                            seed = random.nextLong();
                        }
                        long storageId = storage.newId();
                        storage.write(new VanillaContainer(storageId, NamespacedKey.fromString(dataContainer.get(loottableKey, PersistentDataType.STRING)), seed));
                        dataContainer.set(JustLootItKey.identity(), PersistentDataType.LONG, seed);
                    } else {
                        ItemStack[] items = extractContents(dataContainer);
                        for (int i = 0; i < items.length; i++) {
                            ItemStack item = items[i];
                            if (items[i] == null || !item.getType().isAir()) {
                                continue;
                            }
                            items[i] = null;
                        }
                        long storageId = storage.newId();
                        storage.write(new StaticContainer(storageId, items));
                        dataContainer.set(JustLootItKey.identity(), PersistentDataType.LONG, storageId);
                    }
                } finally {
                    clearLootinKeys(dataContainer);
                    chunk.updateEntity(entity);
                }
            }
        }
    }
    
    private void clearLootinKeys(PersistentDataContainer container) {
        NamespacedKey[] keys = container.getKeys().toArray(NamespacedKey[]::new);
        for (NamespacedKey key : keys) {
            if (!key.getNamespace().equals("lootin")) {
                continue;
            }
            container.remove(key);
        }
    }

    private boolean hasUnsupportedKey(PersistentDataContainer container) {
        return container.has(rwgIdentityKey, PersistentDataType.BYTE) || container.has(rwgLoottableKey, PersistentDataType.STRING)
            || container.has(betterStructuresKey, PersistentDataType.STRING)
            || container.has(customStructuresKey, PersistentDataType.STRING);
    }

    private ItemStack[] extractContents(PersistentDataContainer container) {
        if (container.has(dataKey, ITEM_STACK_ARRAY_TYPE)) {
            return container.get(dataKey, ITEM_STACK_ARRAY_TYPE);
        } else if (container.has(dataKey, PersistentDataType.STRING)) {
            try (
                ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    Base64Coder.decodeLines(container.get(dataKey, PersistentDataType.STRING)));
                BukkitObjectInputStream objectInputStream = new BukkitObjectInputStream(inputStream)) {
                ItemStack[] items = new ItemStack[objectInputStream.readInt()];
                for (int i = 0; i < items.length; i++) {
                    ItemStack item = (ItemStack) objectInputStream.readObject();
                    if (item != null && (item.isSimilar(airPlaceholderItem) || item.getType().isAir())) {
                        items[i] = null;
                        continue;
                    }
                    items[i] = item;
                }
                return items;
            } catch (IOException | ClassNotFoundException e) {
                logger.warning("Failed to deserialize lootin content", e);
                return new ItemStack[0];
            }
        }
        return null;
    }

    @Override
    boolean isEnabled() {
        return properties.isProperty(ConvProp.DO_LOOTIN_CONVERSION);
    }
    
    @Override
    boolean isEnabledFor(ProtoWorld world) {
        return world.hasCapability(StorageCapability.class);
    }

}
