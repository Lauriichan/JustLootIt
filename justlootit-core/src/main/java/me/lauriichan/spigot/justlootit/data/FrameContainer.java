package me.lauriichan.spigot.justlootit.data;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import io.netty.buffer.ByteBuf;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.spigot.justlootit.JustLootItConstant;
import me.lauriichan.spigot.justlootit.config.world.WorldConfig;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.io.IOHandler;
import me.lauriichan.spigot.justlootit.nms.util.RegistryUtil;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;
import me.lauriichan.spigot.justlootit.storage.StorageAdapterRegistry;
import me.lauriichan.spigot.justlootit.util.BlockUtil;

public final class FrameContainer extends Container {

    public static final StorageAdapter<FrameContainer> ADAPTER = new BaseAdapter<>(FrameContainer.class, 14) {
        private final IOHandler<ItemStack> itemIO = DataIO.find(ItemStack.class);

        @Override
        protected void serializeSpecial(final StorageAdapterRegistry registry, final FrameContainer storable, final ByteBuf buffer) {
            itemIO.serialize(buffer, storable.item);
        }

        @Override
        protected FrameContainer deserializeSpecial(final StorageAdapterRegistry registry, final ContainerData data, final ByteBuf buffer) {
            IOHandler.Result<ItemStack> item = itemIO.deserialize(buffer);
            FrameContainer container = new FrameContainer(data, item.value());
            if (item.dirty()) {
                container.setDirty();
            }
            return container;
        }
    };

    private final ItemStack item;
    private final String refreshId;

    public FrameContainer(final ItemStack item) {
        this.item = item == null ? null : item.clone();
        this.refreshId = refreshIdForItem(item);
    }

    private FrameContainer(final ContainerData data, final ItemStack item) {
        super(data);
        this.item = item;
        this.refreshId = refreshIdForItem(item);
    }

    private String refreshIdForItem(ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        NamespacedKey type = RegistryUtil.getKey(itemStack.getType());
        return JustLootItConstant.FRAME_CONTAINER_REFRESH_KEY_FORMAT.formatted(type.getNamespace(), type.getKey());
    }

    @Override
    protected String containerBasedGroupId(WorldConfig worldConfig) {
        if (refreshId == null) {
            return null;
        }
        return worldConfig.getLootTableRefreshGroupId(JustLootItConstant.PLUGIN_NAMESPACE, refreshId);
    }

    public ItemStack getItem() {
        return item;
    }

    public ItemStack generateItem(PlayerAdapter player, Location location) {
        return lootModifications.applyModifications(this, player, location, item.clone(), null, generateSeed(location.getWorld(), player, BlockUtil.getSeed(location)));
    }

    @Override
    public ItemEditor createIcon() {
        return ItemEditor.of(Material.ITEM_FRAME).setName("&cFrame");
    }

}
