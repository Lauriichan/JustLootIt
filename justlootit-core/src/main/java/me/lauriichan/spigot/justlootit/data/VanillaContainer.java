package me.lauriichan.spigot.justlootit.data;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.loot.LootTable;

import io.netty.buffer.ByteBuf;
import me.lauriichan.spigot.justlootit.data.io.NamespacedKeyIO;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;

public final class VanillaContainer extends Container {

    public static final StorageAdapter<VanillaContainer> ADAPTER = new Adapter();

    private static final class Adapter extends StorageAdapter<VanillaContainer> {

        private final NamespacedKeyIO keyIO = NamespacedKeyIO.NAMESPACED_KEY;

        private Adapter() {
            super(VanillaContainer.class, 15);
        }

        @Override
        public void serialize(VanillaContainer storable, ByteBuf buffer) {
            keyIO.serialize(buffer, storable.lootTableKey);
            buffer.writeLong(storable.seed);
        }

        @Override
        public VanillaContainer deserialize(long id, ByteBuf buffer) {
            NamespacedKey key = keyIO.deserialize(buffer);
            long seed = buffer.readLong();
            return new VanillaContainer(id, key, seed);
        }

    }

    private final NamespacedKey lootTableKey;
    private final long seed;

    public VanillaContainer(long id, LootTable lootTable, long seed) {
        this(id, lootTable.getKey(), seed);
    }

    public VanillaContainer(long id, NamespacedKey lootTableKey, long seed) {
        super(id);
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
