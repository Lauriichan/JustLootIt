package me.lauriichan.spigot.justlootit.data;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import io.netty.buffer.ByteBuf;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.nms.io.IOHandler;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;

public final class FrameContainer extends Container {

    public static final StorageAdapter<FrameContainer> ADAPTER = new BaseAdapter<>(FrameContainer.class, 14) {
        private final IOHandler<ItemStack> itemIO = DataIO.find(ItemStack.class);

        @Override
        protected void serializeSpecial(final FrameContainer storable, final ByteBuf buffer) {
            itemIO.serialize(buffer, storable.item);
        }

        @Override
        protected FrameContainer deserializeSpecial(final long id, final ContainerData data, final ByteBuf buffer) {
            IOHandler.Result<ItemStack> item = itemIO.deserialize(buffer); 
            FrameContainer container = new FrameContainer(id, data, item.value());
            if (item.dirty()) {
                container.setDirty();
            }
            return container;
        }
    };

    private final ItemStack item;

    public FrameContainer(final long id, final ItemStack item) {
        super(id);
        this.item = item == null ? null : item.clone();
    }

    private FrameContainer(final long id, final ContainerData data, final ItemStack item) {
        super(id, data);
        this.item = item;
    }

    public ItemStack getItem() {
        return item;
    }

    @Override
    public ItemEditor createIcon() {
        return ItemEditor.of(Material.ITEM_FRAME).setName("&cFrame");
    }

}
