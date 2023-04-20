package me.lauriichan.spigot.justlootit.data;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.netty.buffer.ByteBuf;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.io.IOHandler;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;

public final class StaticContainer extends Container implements IInventoryContainer {

    public static final StorageAdapter<StaticContainer> ADAPTER = new BaseAdapter<>(StaticContainer.class, 15) {
        private final IOHandler<ItemStack> itemIO = DataIO.find(ItemStack.class);

        @Override
        protected void serializeSpecial(StaticContainer storable, ByteBuf buffer) {
            itemIO.serializeArray(buffer, storable.items);
        }

        @Override
        protected StaticContainer deserializeSpecial(long id, ContainerData data, ByteBuf buffer) {
            return new StaticContainer(id, data, itemIO.deserializeArray(buffer));
        }
    };

    private ItemStack[] items;

    public StaticContainer(long id, final Inventory inventory) {
        super(id);
        saveFrom(inventory);
    }

    private StaticContainer(long id, ContainerData data, final ItemStack[] items) {
        super(id, data);
        this.items = items;
    }

    public ItemStack[] getItems() {
        return items;
    }

    public void loadTo(Inventory inventory) {
        int size = Math.min(inventory.getSize(), items.length);
        for (int index = 0; index < size; index++) {
            ItemStack item = items[index];
            if (item == null) {
                inventory.clear(index);
                continue;
            }
            inventory.setItem(index, item.clone());
        }
    }

    public void saveFrom(Inventory inventory) {
        ItemStack[] contents = inventory.getContents();
        ItemStack[] items = new ItemStack[contents.length];
        for (int index = 0; index < contents.length; index++) {
            ItemStack item = contents[index];
            if (item == null || item.getType().isAir()) {
                items[index] = null;
                continue;
            }
            contents[index] = item.clone();
        }
        this.items = items;
        setDirty();
    }

    @Override
    public void fill(PlayerAdapter player, Location location, Inventory inventory) {
        loadTo(inventory);
    }

}
