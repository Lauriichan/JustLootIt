package me.lauriichan.spigot.justlootit.data;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.loot.LootTable;

import io.netty.buffer.ByteBuf;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;

public final class VanillaContainer extends Container implements IInventoryContainer {

    public static final StorageAdapter<VanillaContainer> ADAPTER = new BaseAdapter<>(VanillaContainer.class, 16) {
        @Override
        protected void serializeSpecial(final VanillaContainer storable, final ByteBuf buffer) {
            DataIO.NAMESPACED_KEY.serialize(buffer, storable.lootTableKey);
            buffer.writeLong(storable.seed);
        }

        @Override
        protected VanillaContainer deserializeSpecial(final long id, final ContainerData data, final ByteBuf buffer) {
            final NamespacedKey key = DataIO.NAMESPACED_KEY.deserialize(buffer);
            final long seed = buffer.readLong();
            return new VanillaContainer(id, data, key, seed);
        }
    };

    private NamespacedKey lootTableKey;
    private long seed;

    public VanillaContainer(final long id, final LootTable lootTable, final long seed) {
        super(id);
        this.lootTableKey = lootTable.getKey();
        this.seed = seed;
    }

    private VanillaContainer(final long id, final ContainerData data, final NamespacedKey lootTableKey, final long seed) {
        super(id, data);
        this.lootTableKey = lootTableKey;
        this.seed = seed;
    }

    public NamespacedKey getLootTableKey() {
        return lootTableKey;
    }

    public LootTable getLootTable() {
        return Bukkit.getLootTable(lootTableKey);
    }

    public void setLootTable(final LootTable lootTable) {
        this.lootTableKey = Objects.requireNonNull(lootTable, "LootTable can't be null").getKey();
        setDirty();
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(final long seed) {
        this.seed = seed;
        setDirty();
    }

    @Override
    public void fill(final PlayerAdapter player, final Location location, final Inventory inventory) {

    }

}
