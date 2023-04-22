package me.lauriichan.spigot.justlootit.data;

import org.bukkit.Location;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.netty.buffer.ByteBuf;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.inventory.ChestSize;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.io.IOHandler;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;

public final class StaticContainer extends Container implements IInventoryContainer {

    public static final StorageAdapter<StaticContainer> ADAPTER = new BaseAdapter<>(StaticContainer.class, 15) {
        private final IOHandler<ItemStack> itemIO = DataIO.find(ItemStack.class);

        @Override
        protected void serializeSpecial(final StaticContainer storable, final ByteBuf buffer) {
            itemIO.serializeArray(buffer, storable.items);
        }

        @Override
        protected StaticContainer deserializeSpecial(final long id, final ContainerData data, final ByteBuf buffer) {
            return new StaticContainer(id, data, itemIO.deserializeArray(buffer));
        }
    };

    private ItemStack[] items;

    public StaticContainer(final long id, final Inventory inventory) {
        super(id);
        saveFrom(inventory);
    }

    private StaticContainer(final long id, final ContainerData data, final ItemStack[] items) {
        super(id, data);
        this.items = items;
    }

    public ItemStack[] getItems() {
        return items;
    }

    public void loadTo(final Inventory inventory) {
        final int size = Math.min(inventory.getSize(), items.length);
        for (int index = 0; index < size; index++) {
            final ItemStack item = items[index];
            if (item == null) {
                inventory.clear(index);
                continue;
            }
            inventory.setItem(index, item.clone());
        }
    }

    public void saveFrom(final Inventory inventory) {
        final ItemStack[] items;
        if (inventory instanceof DoubleChestInventory doubleChest) {
            items = new ItemStack[ChestSize.GRID_6x9.inventorySize()];
            copyTo(doubleChest.getLeftSide().getContents(), items, 0);
            copyTo(doubleChest.getRightSide().getContents(), items, ChestSize.GRID_3x9.inventorySize());
        } else {
            items = new ItemStack[inventory.getSize()];
            copyTo(inventory.getContents(), items, 0);
        }
        this.items = items;
        setDirty();
    }

    private void copyTo(ItemStack[] from, ItemStack[] to, int offset) {
        for (int index = 0; index < from.length; index++) {
            final ItemStack item = from[index];
            if (item == null || item.getType().isAir()) {
                to[index + offset] = null;
                continue;
            }
            to[index + offset] = item.clone();
        }
    }

    @Override
    public void fill(final PlayerAdapter player, final Location location, final Inventory inventory) {
        loadTo(inventory);
    }

}
