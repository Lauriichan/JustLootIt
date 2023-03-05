package me.lauriichan.spigot.justlootit.data;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.loot.LootTable;

import io.netty.buffer.ByteBuf;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;

public final class VanillaContainer extends Container {

    public static final StorageAdapter<VanillaContainer> ADAPTER = new BaseAdapter<>(VanillaContainer.class, 16) {
        @Override
        protected void serializeSpecial(VanillaContainer storable, ByteBuf buffer) {
            DataIO.NAMESPACED_KEY.serialize(buffer, storable.lootTableKey);
            buffer.writeLong(storable.seed);
        }

        @Override
        protected VanillaContainer deserializeSpecial(long id, ContainerData data, ByteBuf buffer) {
            NamespacedKey key = DataIO.NAMESPACED_KEY.deserialize(buffer);
            long seed = buffer.readLong();
            return new VanillaContainer(id, data, key, seed);
        }
    };

    private final NamespacedKey lootTableKey;
    private final long seed;

    public VanillaContainer(long id, LootTable lootTable, long seed) {
        super(id);
        this.lootTableKey = lootTable.getKey();
        this.seed = seed;
    }

    private VanillaContainer(long id, ContainerData data, NamespacedKey lootTableKey, long seed) {
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

    public long getSeed() {
        return seed;
    }

}
