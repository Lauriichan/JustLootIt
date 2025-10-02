package me.lauriichan.spigot.justlootit.data;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.loot.LootTable;

import io.netty.buffer.ByteBuf;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventory;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.api.event.player.JLIPlayerVanillaLootGenerateEvent;
import me.lauriichan.spigot.justlootit.api.event.player.JLIPlayerVanillaLootProvidedEvent;
import me.lauriichan.spigot.justlootit.capability.ActorCapability;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.message.Messages;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;
import me.lauriichan.spigot.justlootit.storage.StorageAdapterRegistry;

public final class VanillaContainer extends Container implements IInventoryContainer {
    
    public static final record VanillaResult(LootTable lootTable, long seed) implements IResult {}

    public static final StorageAdapter<VanillaContainer> ADAPTER = new BaseAdapter<>(VanillaContainer.class, 16) {
        @Override
        protected void serializeSpecial(final StorageAdapterRegistry registry, final VanillaContainer storable, final ByteBuf buffer) {
            DataIO.NAMESPACED_KEY.serialize(buffer, storable.lootTableKey);
            buffer.writeLong(storable.seed);
        }

        @Override
        protected VanillaContainer deserializeSpecial(final StorageAdapterRegistry registry, final ContainerData data, final ByteBuf buffer) {
            final NamespacedKey key = DataIO.NAMESPACED_KEY.deserialize(buffer).value();
            final long seed = buffer.readLong();
            return new VanillaContainer(data, key, seed);
        }
    };

    private NamespacedKey lootTableKey;
    private long seed;

    public VanillaContainer(final LootTable lootTable, final long seed) {
        this(lootTable.getKey(), seed);
    }

    public VanillaContainer(final NamespacedKey lootTableKey, final long seed) {
        this.lootTableKey = lootTableKey;
        this.seed = seed;
    }

    private VanillaContainer(final ContainerData data, final NamespacedKey lootTableKey, final long seed) {
        super(data);
        this.lootTableKey = lootTableKey;
        this.seed = seed;
    }

    public NamespacedKey getLootTableKey() {
        return lootTableKey;
    }
    
    public void setLootTableKey(final NamespacedKey lootTableKey) {
        this.lootTableKey = Objects.requireNonNull(lootTableKey, "LootTable key can't be null");
        setDirty();
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
    public IResult fill(final PlayerAdapter player, final InventoryHolder holder, final Location location, final Inventory inventory) {
        LootTable table = getLootTable();
        if (table == null) {
            ActorCapability.actor(player).sendTranslatedMessage(Messages.CONTAINER_VANILLA_LOOTTABLE_NOT_AVAILABLE,
                Key.of("lootTable", getLootTableKey()));
            return IResult.empty();
        }
        JLIPlayerVanillaLootGenerateEvent event = new JLIPlayerVanillaLootGenerateEvent((JustLootItPlugin) player.versionHandler().plugin(), player, getLootTable(), generateSeed(player, seed));
        event.call().join();
        player.versionHandler().versionHelper().fill(inventory, player.asBukkit(), location, event.lootTable(), event.seed());
        return new VanillaResult(event.lootTable(), event.seed());
    }
    
    @Override
    public void awaitProvidedEvent(PlayerAdapter player, IGuiInventory inventory, InventoryHolder entryHolder, Location entryLocation,
        IResult result) {
        // This does not use the loot table and seed set by the previous event
        // This should be kept in mind when using it
        if (result instanceof VanillaResult vanillaResult) {
            new JLIPlayerVanillaLootProvidedEvent((JustLootItPlugin) player.versionHandler().plugin(), player, inventory, entryHolder,
                entryLocation, vanillaResult.lootTable(), vanillaResult.seed()).call().join();
            return;
        }
        new JLIPlayerVanillaLootProvidedEvent((JustLootItPlugin) player.versionHandler().plugin(), player, inventory, entryHolder,
            entryLocation, getLootTable(), seed).call().join();
    }

    @Override
    public ItemEditor createIcon() {
        return ItemEditor.of(Material.ENDER_CHEST).setName("&cVanilla");
    }

}
