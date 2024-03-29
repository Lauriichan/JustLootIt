package me.lauriichan.spigot.justlootit.data;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.netty.buffer.ByteBuf;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.io.IOHandler;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;
import me.lauriichan.spigot.justlootit.util.InventoryUtil;

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
    
    public int amount() {
        return items.length;
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
        final ItemStack[] contents = InventoryUtil.getContents(inventory);
        final ItemStack[] items = new ItemStack[contents.length];
        for (int index = 0; index < contents.length; index++) {
            final ItemStack item = contents[index];
            if (item == null || item.getType().isAir()) {
                items[index] = null;
                continue;
            }
            items[index] = item.clone();
        }
        this.items = items;
        setDirty();
    }

    @Override
    public void fill(final PlayerAdapter player, final Location location, final Inventory inventory) {
        loadTo(inventory);
    }

    @Override
    public ItemEditor createIcon() {
        return ItemEditor.of(Material.CHEST).setName("&cStatic");
    }

}
