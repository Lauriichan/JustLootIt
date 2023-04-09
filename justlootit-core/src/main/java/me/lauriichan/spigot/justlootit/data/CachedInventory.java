package me.lauriichan.spigot.justlootit.data;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.netty.buffer.ByteBuf;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.nms.io.IOHandler;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;

public final class CachedInventory extends Storable {

    public static final StorageAdapter<CachedInventory> ADAPTER = new StorageAdapter<>(CachedInventory.class, 0) {
        private final IOHandler<ItemStack> itemIO = DataIO.find(ItemStack.class);

        @Override
        public void serialize(CachedInventory storable, ByteBuf buffer) {
            itemIO.serializeArray(buffer, storable.items);
        }

        @Override
        public CachedInventory deserialize(long id, ByteBuf buffer) {
            return new CachedInventory(id, itemIO.deserializeArray(buffer));
        }

    };

    private final ItemStack[] items;

    public CachedInventory(long id, Inventory inventory) {
        super(id);
        this.items = inventory.getContents();
    }

    private CachedInventory(long id, ItemStack[] items) {
        super(id);
        this.items = items;
    }

    public ItemStack[] getItems() {
        return items;
    }

}
