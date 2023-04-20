package me.lauriichan.spigot.justlootit.data;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.netty.buffer.ByteBuf;
import me.lauriichan.spigot.justlootit.data.io.BufIO;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.nms.io.IOHandler;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;

public final class CachedInventory extends Storable {

    public static final StorageAdapter<CachedInventory> ADAPTER = new StorageAdapter<>(CachedInventory.class, 0) {
        private final IOHandler<ItemStack> itemIO = DataIO.find(ItemStack.class);

        @Override
        public void serialize(final CachedInventory storable, final ByteBuf buffer) {
            BufIO.writeString(buffer, storable.type.name());
            itemIO.serializeArray(buffer, storable.items);
        }

        @Override
        public CachedInventory deserialize(final long id, final ByteBuf buffer) {
            final InventoryType type = fromString(BufIO.readString(buffer));
            final ItemStack[] items = itemIO.deserializeArray(buffer);
            return new CachedInventory(id, items, type);
        }

        private InventoryType fromString(final String string) {
            try {
                return InventoryType.valueOf(string);
            } catch (final IllegalArgumentException exception) {
                return InventoryType.CHEST;
            }
        }

    };

    private final ItemStack[] items;
    private final InventoryType type;

    public CachedInventory(final long id, final Inventory inventory) {
        super(id);
        final ItemStack[] contents = inventory.getContents();
        int index = 0;
        final ItemStack[] items = new ItemStack[contents.length];
        for (final ItemStack item : contents) {
            if (item == null || item.getType().isAir()) {
                items[index++] = null;
                continue;
            }
            items[index++] = item.clone();
        }
        this.items = items;
        this.type = inventory.getType();
    }

    private CachedInventory(final long id, final ItemStack[] items, final InventoryType type) {
        super(id);
        this.items = items;
        this.type = type;
    }

    public InventoryType getType() {
        return type;
    }

    public ItemStack[] getItems() {
        return items;
    }

    public int size() {
        return items.length;
    }

}
