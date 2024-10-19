package me.lauriichan.spigot.justlootit.data;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.netty.buffer.ByteBuf;
import me.lauriichan.spigot.justlootit.data.io.BufIO;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.nms.io.IOHandler;
import me.lauriichan.spigot.justlootit.storage.IModifiable;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;
import me.lauriichan.spigot.justlootit.storage.StorageAdapterRegistry;

public final class CachedInventory implements IModifiable {

    public static final StorageAdapter<CachedInventory> ADAPTER = new StorageAdapter<>(CachedInventory.class, 0) {
        private final IOHandler<ItemStack> itemIO = DataIO.find(ItemStack.class);

        @Override
        public void serialize(final StorageAdapterRegistry registry, final CachedInventory storable, final ByteBuf buffer) {
            BufIO.writeString(buffer, storable.type.name());
            itemIO.serializeArray(buffer, storable.items);
        }

        @Override
        public CachedInventory deserialize(final StorageAdapterRegistry registry, final ByteBuf buffer) {
            final InventoryType type = fromString(BufIO.readString(buffer));
            final IOHandler.Result<ItemStack[]> items = itemIO.deserializeArray(buffer);
            return new CachedInventory(type, items.value(), items.dirty());
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
    
    private final boolean dirty;

    public CachedInventory(final Inventory inventory) {
        final ItemStack[] contents = inventory.getContents();
        final ItemStack[] items = new ItemStack[contents.length];
        for (int index = 0; index < contents.length; index++) {
            ItemStack item = contents[index];
            if (item == null || item.getType().isAir()) {
                items[index] = null;
                continue;
            }
            items[index] = item.clone();
        }
        this.type = inventory.getType();
        this.items = items;
        this.dirty = true;
    }

    private CachedInventory(final InventoryType type, final ItemStack[] items, final boolean dirty) {
        this.type = type;
        this.items = items;
        this.dirty = dirty;
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
    
    @Override
    public boolean isDirty() {
        return dirty;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof CachedInventory other) {
            if (other.type != type || other.items.length != items.length) {
                return false;
            }
            ItemStack is1, is2;
            for (int i = 0; i < items.length; i++) {
                is1 = items[i];
                is2 = other.items[i];
                if ((is1 == null || is2 == null) && is1 != is2) {
                    return false;
                }
                if (is1.getAmount() != is2.getAmount() && is1.isSimilar(is2)) {
                    return false;
                }
            }
            return true;
        } else if (obj instanceof Inventory inventory) {
            if (type != inventory.getType() || items.length != inventory.getSize()) {
                return false;
            }
            ItemStack[] contents = inventory.getContents();
            ItemStack is1, is2;
            for (int i = 0; i < contents.length; i++) {
                is1 = items[i];
                is2 = contents[i];
                if (is2 != null && is2.getType().isAir()) {
                    is2 = null;
                }
                if (is1 == null || is2 == null) {
                    if (is1 == is2) {
                        continue;
                    }
                    return false;
                }
                if (is1.getAmount() != is2.getAmount() && !is1.isSimilar(is2)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
