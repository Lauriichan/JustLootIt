package me.lauriichan.spigot.justlootit.convert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.type.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.spigot.justlootit.JustLootItConstant;
import me.lauriichan.spigot.justlootit.JustLootItFlag;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoChunk;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoEntity;
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

    private final ISimpleLogger logger;
    
    public LootinConverter(ISimpleLogger logger, ConversionProperties properties) {
        super(properties);
        this.logger = logger;
    }

    @Override
    public void convert(ProtoChunk chunk) {
        // TODO: Implement
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
                if (!dataContainer.has(identityKey, PersistentDataType.STRING) || hasUnsupportedKey(dataContainer)) {
                    return;
                }
                if (state.getBlockData() instanceof Chest chest) {
                    Chest.Type type = chest.getType();

                }
                try {
                    dataContainer.remove(identityKey);

                } finally {
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
                    dataContainer.remove(elytraKey);
                    // TODO: Create frame container (we know it's an elytra cause Lootin doesn't support any other items)
                } finally {
                    chunk.updateEntity(entity);
                }
            } else if (EntityUtil.isSuppportedEntity(entity.getType())) {
                PersistentDataContainer dataContainer = entity.getContainer();
                if (!dataContainer.has(identityKey, PersistentDataType.STRING) || hasUnsupportedKey(dataContainer)) {
                    return;
                }
                try {
                    dataContainer.remove(identityKey);
                    // TODO: Create loot table container or static container based on information (same as with block entities)
                } finally {
                    chunk.updateEntity(entity);
                }
            }
        }
    }

    private boolean hasUnsupportedKey(PersistentDataContainer container) {
        return container.has(rwgIdentityKey, PersistentDataType.BYTE) || container.has(rwgLoottableKey, PersistentDataType.STRING)
            || container.has(betterStructuresKey, PersistentDataType.STRING)
            || container.has(customStructuresKey, PersistentDataType.STRING);
    }

    private ItemStack[] extractContents(PersistentDataContainer container) {
        if (container.has(loottableKey, ITEM_STACK_ARRAY_TYPE)) {
            return container.get(loottableKey, ITEM_STACK_ARRAY_TYPE);
        } else if (container.has(loottableKey, PersistentDataType.STRING)) {
            try (
                ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    Base64Coder.decodeLines(container.get(loottableKey, PersistentDataType.STRING)));
                BukkitObjectInputStream objectInputStream = new BukkitObjectInputStream(inputStream)) {
                ItemStack[] items = new ItemStack[objectInputStream.readInt()];
                for (int i = 0; i < items.length; i++) {
                    ItemStack item = items[i];
                    if (item != null && item.isSimilar(airPlaceholderItem)) {
                        item = new ItemStack(Material.AIR);
                    }
                    items[i] = item;
                }
                return items;
            } catch (IOException e) {
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

}
