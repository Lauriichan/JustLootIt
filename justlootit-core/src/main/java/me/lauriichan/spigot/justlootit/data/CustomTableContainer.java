package me.lauriichan.spigot.justlootit.data;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import io.netty.buffer.ByteBuf;
import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventory;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.spigot.justlootit.config.world.WorldConfig;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;
import me.lauriichan.spigot.justlootit.storage.StorageAdapterRegistry;

public class CustomTableContainer extends Container implements IInventoryContainer {

    public static final record CustomTableResult(long seed) implements IResult {}

    public static final StorageAdapter<CustomTableContainer> ADAPTER = new BaseAdapter<>(CustomTableContainer.class, 18) {

        @Override
        protected void serializeSpecial(StorageAdapterRegistry registry, CustomTableContainer storable, ByteBuf buffer) {

        }

        @Override
        protected CustomTableContainer deserializeSpecial(StorageAdapterRegistry registry, ContainerData data, ByteBuf buffer) {
            return null;
        }

    };

    private NamespacedKey lootTableKey;
    private long seed;

    public CustomTableContainer(final long seed) {

    }
    
    @Override
    protected String containerBasedGroupId(WorldConfig worldConfig) {
        return worldConfig.getLootTableRefreshGroupId(lootTableKey);
    }

    @Override
    public IResult fill(PlayerAdapter player, InventoryHolder holder, Location location, Inventory inventory) {
        // TODO Auto-generated method stub
        return IInventoryContainer.super.fill(player, holder, location, inventory);
    }

    @Override
    public void awaitProvidedEvent(PlayerAdapter player, IGuiInventory inventory, InventoryHolder entryHolder, Location entryLocation,
        IResult result) {
        // TODO Auto-generated method stub
        IInventoryContainer.super.awaitProvidedEvent(player, inventory, entryHolder, entryLocation, result);
    }

    @Override
    public ItemEditor createIcon() {
        return ItemEditor.of(Material.BUDDING_AMETHYST).setName("&dCustom Table");
    }

}
