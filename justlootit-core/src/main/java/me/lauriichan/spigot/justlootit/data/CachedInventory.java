package me.lauriichan.spigot.justlootit.data;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.netty.buffer.ByteBuf;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.nms.io.IOHandler;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;

public final class CachedInventory extends Storable {

    public static final StorageAdapter<CachedInventory> ADAPTER = new Adapter();

    private static final class Adapter extends StorageAdapter<CachedInventory> {

        @SuppressWarnings("unchecked")
        private final IOHandler<ItemStack> itemIO = (IOHandler<ItemStack>) JustLootItPlugin.get().versionHandler().io()
            .handlerOf(ItemStack.class);

        public Adapter() {
            super(CachedInventory.class, 1);
        }

        @Override
        public void serialize(CachedInventory storable, ByteBuf buffer) {
            itemIO.serializeArray(buffer, storable.items);
        }

        @Override
        public CachedInventory deserialize(long id, ByteBuf buffer) {
            ItemStack[] items = itemIO.deserializeArray(buffer);
            return new CachedInventory(id, items);
        }

    }

    private final ItemStack[] items;

    public CachedInventory(long id, final Inventory inventory) {
        this(id, inventory.getContents());
    }

    public CachedInventory(long id, final ItemStack[] items) {
        super(id);
        this.items = items;
    }

    public ItemStack[] getItems() {
        return items;
    }

    public void restore(Inventory inventory) {
        inventory.setContents(items);
    }

}
