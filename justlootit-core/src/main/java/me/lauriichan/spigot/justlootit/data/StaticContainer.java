package me.lauriichan.spigot.justlootit.data;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventory;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.spigot.justlootit.JustLootItConstant;
import me.lauriichan.spigot.justlootit.config.world.WorldConfig;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoBlockEntity;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoEntity;
import me.lauriichan.spigot.justlootit.nms.io.IOHandler;
import me.lauriichan.spigot.justlootit.nms.nbt.ICompoundTag;
import me.lauriichan.spigot.justlootit.nms.nbt.IListTag;
import me.lauriichan.spigot.justlootit.nms.nbt.NbtHelper;
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;
import me.lauriichan.spigot.justlootit.storage.StorageAdapterRegistry;
import me.lauriichan.spigot.justlootit.util.EntityUtil;
import me.lauriichan.spigot.justlootit.util.InventoryUtil;

public final class StaticContainer extends Container implements IInventoryContainer {

    public static final StorageAdapter<StaticContainer> ADAPTER = new BaseAdapter<>(StaticContainer.class, 15) {
        private final IOHandler<ItemStack> itemIO = DataIO.find(ItemStack.class);

        @Override
        protected void serializeSpecial(final StorageAdapterRegistry registry, final StaticContainer storable, final ByteBuf buffer) {
            itemIO.serializeArray(buffer, storable.items);
        }

        @Override
        protected StaticContainer deserializeSpecial(final StorageAdapterRegistry registry, final ContainerData data,
            final ByteBuf buffer) {
            IOHandler.Result<ItemStack[]> items = itemIO.deserializeArray(buffer);
            StaticContainer container = new StaticContainer(data, items.value());
            if (items.dirty()) {
                container.setDirty();
            }
            return container;
        }
    };

    private ItemStack[] items;

    private volatile ItemStack[] editingItems = null;
    private final ObjectArrayList<IGuiInventory> editingInventories = new ObjectArrayList<>();

    public StaticContainer(final Inventory inventory) {
        saveFrom(inventory);
    }

    public StaticContainer(final ItemStack[] contents) {
        saveFrom(contents);
    }

    private StaticContainer(final ContainerData data, final ItemStack[] items) {
        super(data);
        this.items = items;
    }
    
    @Override
    protected String containerBasedGroupId(WorldConfig worldConfig) {
        return worldConfig.getLootTableRefreshGroupId(JustLootItConstant.PLUGIN_NAMESPACE, JustLootItConstant.STATIC_CONTAINER_REFRESH_KEY);
    }

    public void pushEditor(IGuiInventory inventory) {
        if (editingItems != null) {
            inventory.getInventory().setContents(editingItems);
        } else {
            loadTo(inventory.getInventory());
        }
        if (editingInventories.contains(inventory)) {
            return;
        }
        editingInventories.push(inventory);
    }

    public boolean popEditor(IGuiInventory inventory) {
        if (editingInventories.remove(inventory) && editingInventories.isEmpty()) {
            editingItems = null;
            return true;
        }
        return false;
    }

    public void pubEdit(IGuiInventory inventory) {
        editingItems = inventory.getInventory().getContents();
        for (int i = 0; i < editingInventories.size(); i++) {
            IGuiInventory other = editingInventories.get(i);
            if (other == inventory) {
                continue;
            }
            other.update();
        }
    }

    public int amount() {
        return items.length;
    }

    public ItemStack[] getItems() {
        return items;
    }

    public boolean isSame(final Inventory inventory) {
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
        saveFrom(InventoryUtil.getContents(inventory));
    }

    public void saveFrom(final ItemStack[] contents) {
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
    public void fillNoResult(final PlayerAdapter player, final InventoryHolder holder, final Location location, final Inventory inventory) {
        loadTo(inventory);
    }

    @Override
    public void restore(ISimpleLogger logger, VersionHandler versionHandler, ProtoBlockEntity entity) {
        loadTo(entity.getInventory());
    }

    @Override
    public void restore(ISimpleLogger logger, VersionHandler versionHandler, ProtoEntity entity) {
        NbtHelper helper = versionHandler.nbtHelper();
        ICompoundTag root = entity.getNbt();
        IListTag<ICompoundTag> listTag = root.getList("Items", TagType.COMPOUND);
        if (listTag == null) {
            listTag = helper.createList(TagType.COMPOUND);
            root.set("Items", listTag);
        } else {
            listTag.clear();
        }
        final int size = Math.min(EntityUtil.getInventorySize(entity.getType()), items.length);
        for (int index = 0; index < size; index++) {
            final ItemStack item = items[index];
            if (item == null) {
                continue;
            }
            ICompoundTag tag = helper.asTag(item);
            tag.set("Slot", index & 0xFF);
            listTag.add(tag);
        }
    }

    @Override
    public ItemEditor createIcon() {
        return ItemEditor.of(Material.CHEST).setName("&cStatic");
    }

}
