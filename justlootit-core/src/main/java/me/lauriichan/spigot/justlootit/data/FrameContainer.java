package me.lauriichan.spigot.justlootit.data;

import org.bukkit.inventory.ItemStack;

import io.netty.buffer.ByteBuf;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.nms.io.IOHandler;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;

public final class FrameContainer extends Container {

    public static final StorageAdapter<FrameContainer> ADAPTER = new BaseAdapter<>(FrameContainer.class, 14) {
        private final IOHandler<ItemStack> itemIO = DataIO.find(ItemStack.class);

        @Override
        protected void serializeSpecial(FrameContainer storable, ByteBuf buffer) {
            itemIO.serialize(buffer, storable.item);
        }

        @Override
        protected FrameContainer deserializeSpecial(long id, ContainerData data, ByteBuf buffer) {
            return new FrameContainer(id, data, itemIO.deserialize(buffer));
        }
    };

    private ItemStack item;

    public FrameContainer(long id, final ItemStack item) {
        super(id);
        this.item = item == null ? null : item.clone();
    }

    private FrameContainer(long id, final ContainerData data, final ItemStack item) {
        super(id, data);
        this.item = item;
    }

    public ItemStack getItem() {
        return item;
    }

}
