package me.lauriichan.spigot.justlootit.convert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Array;

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

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.spigot.justlootit.JustLootItConstant;
import me.lauriichan.spigot.justlootit.JustLootItFlag;
import me.lauriichan.spigot.justlootit.config.ConversionConfig;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoChunk;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoEntity;
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

    public LootinConverter(ConversionConfig config) {
        super(config);
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
                if (!dataContainer.has(identityKey, PersistentDataType.STRING)) {
                    return;
                }
                if (state.getBlockData() instanceof Chest chest) {
                    Chest.Type type = chest.getType();
                    
                }
                try {
                    dataContainer.remove(identityKey);

                } finally {
                    container.update();
                }
            }
        }
        for (ProtoEntity entity : chunk.getEntities()) {
            
        }
    }

    @Override
    boolean isEnabled() {
        return config.doLootinConversion();
    }

}
