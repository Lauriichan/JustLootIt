package me.lauriichan.spigot.justlootit.data;

import java.util.Objects;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.loot.LootContext;

import io.netty.buffer.ByteBuf;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventory;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.api.event.player.JLIPlayerCustomLootGenerateEvent;
import me.lauriichan.spigot.justlootit.api.event.player.JLIPlayerVanillaLootProvidedEvent;
import me.lauriichan.spigot.justlootit.capability.ActorCapability;
import me.lauriichan.spigot.justlootit.config.data.CustomLootTable;
import me.lauriichan.spigot.justlootit.config.world.WorldConfig;
import me.lauriichan.spigot.justlootit.data.VanillaContainer.VanillaResult;
import me.lauriichan.spigot.justlootit.data.io.DataIO;
import me.lauriichan.spigot.justlootit.loot.io.LootRegistry;
import me.lauriichan.spigot.justlootit.message.Messages;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;
import me.lauriichan.spigot.justlootit.storage.StorageAdapterRegistry;

public class CustomContainer extends Container implements IInventoryContainer {

    public static final StorageAdapter<CustomContainer> ADAPTER = new BaseAdapter<>(CustomContainer.class, 18) {

        @Override
        protected void serializeSpecial(StorageAdapterRegistry registry, CustomContainer storable, ByteBuf buffer) {
            DataIO.NAMESPACED_KEY.serialize(buffer, storable.lootTableKey);
            buffer.writeLong(storable.seed);
        }

        @Override
        protected CustomContainer deserializeSpecial(StorageAdapterRegistry registry, ContainerData data, ByteBuf buffer) {
            final NamespacedKey key = DataIO.NAMESPACED_KEY.deserialize(buffer).value();
            final long seed = buffer.readLong();
            return new CustomContainer(key, seed);
        }

    };

    private NamespacedKey lootTableKey;
    private long seed;

    public CustomContainer(final NamespacedKey lootTableKey, final long seed) {
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

    public long getSeed() {
        return seed;
    }

    public void setSeed(final long seed) {
        this.seed = seed;
        setDirty();
    }

    @Override
    protected String containerBasedGroupId(WorldConfig worldConfig) {
        return worldConfig.getLootTableRefreshGroupId(lootTableKey);
    }

    @Override
    public IResult fill(PlayerAdapter player, InventoryHolder holder, Location location, Inventory inventory) {
        CustomLootTable table = lootTables.get(lootTableKey);
        if (table == null) {
            ActorCapability.actor(player).sendTranslatedMessage(Messages.CONTAINER_CUSTOM_LOOTTABLE_NOT_AVAILABLE,
                Key.of("lootTable", getLootTableKey()));
            return IResult.failed();
        }

        JLIPlayerCustomLootGenerateEvent event = new JLIPlayerCustomLootGenerateEvent((JustLootItPlugin) player.versionHandler().plugin(),
            player, table, generateSeed(location.getWorld(), player, seed));
        event.call().join();

        Player bktPlayer = player.asBukkit();
        AttributeInstance luckAttr = bktPlayer.getAttribute(LootRegistry.REGISTRY.attrLuck());
        float luck = 0f;
        if (luckAttr != null) {
            luck = (float) luckAttr.getValue();
        }
        event.lootTable().fillInventory(inventory, new Random(event.seed()),
            new LootContext.Builder(location).luck(luck).killer(bktPlayer).build());

        lootModifications.applyModifications(this, player, location, inventory, lootTableKey, event.seed());

        return new VanillaResult(event.lootTable(), event.seed());
    }

    @Override
    public void awaitProvidedEvent(PlayerAdapter player, IGuiInventory inventory, InventoryHolder entryHolder, Location entryLocation,
        IResult result) {
        // This does not use the loot table and seed set by the previous event
        // This should be kept in mind when using it

        // And yes, this is not vanilla but it implements the Bukkit interface :)
        if (result instanceof VanillaResult vanillaResult) {
            new JLIPlayerVanillaLootProvidedEvent((JustLootItPlugin) player.versionHandler().plugin(), player, inventory, entryHolder,
                entryLocation, vanillaResult.lootTable(), vanillaResult.seed()).call().join();
            return;
        }
        new JLIPlayerVanillaLootProvidedEvent((JustLootItPlugin) player.versionHandler().plugin(), player, inventory, entryHolder,
            entryLocation, lootTables.get(lootTableKey), seed).call().join();
    }

    @Override
    public ItemEditor createIcon() {
        return ItemEditor.of(Material.BUDDING_AMETHYST).setName("&dCustom");
    }

}
