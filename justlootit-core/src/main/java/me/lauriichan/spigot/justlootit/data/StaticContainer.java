package me.lauriichan.spigot.justlootit.data;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.netty.buffer.ByteBuf;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.nms.io.IOHandler;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;

public final class StaticContainer extends Container {

    public static final StorageAdapter<StaticContainer> ADAPTER = new Adapter();

    private static final class Adapter extends StorageAdapter<StaticContainer> {

        @SuppressWarnings("unchecked")
        private final IOHandler<ItemStack> itemIO = (IOHandler<ItemStack>) JustLootItPlugin.get().versionHandler().io()
            .handlerOf(ItemStack.class);

        private Adapter() {
            super(StaticContainer.class, 15);
        }

        @Override
        public void serialize(StaticContainer storable, ByteBuf buffer) {
            itemIO.serializeArray(buffer, storable.items);
        }

        @Override
        public StaticContainer deserialize(long id, ByteBuf buffer) {
            return new StaticContainer(id, itemIO.deserializeArray(buffer));
        }

    }

    private final ItemStack[] items;

    public StaticContainer(long id, final Inventory inventory) {
        this(id, inventory.getContents());
    }

    public StaticContainer(long id, final ItemStack[] items) {
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
