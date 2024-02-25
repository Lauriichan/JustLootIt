package me.lauriichan.spigot.justlootit.nms.v1_20_R2;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R2.CraftLootTable;
import org.bukkit.craftbukkit.v1_20_R2.CraftServer;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_20_R2.util.CraftNamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.VersionHelper;
import me.lauriichan.spigot.justlootit.nms.v1_20_R2.io.ItemStackIO1_20_R2;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class VersionHelper1_20_R2 extends VersionHelper {

    private final VersionHandler1_20_R2 handler;

    public VersionHelper1_20_R2(final VersionHandler1_20_R2 handler) {
        this.handler = handler;
    }

    @Override
    public VersionHandler handler() {
        return handler;
    }

    @Override
    public ItemTag asItemTag(final org.bukkit.inventory.ItemStack itemStack) {
        return ItemTag.ofNbt(ItemStackIO1_20_R2.ITEM_STACK.asMinecraftStack(itemStack).getOrCreateTag().getAsString());
    }

    @Override
    public net.md_5.bungee.api.chat.hover.content.Entity createEntityHover(final org.bukkit.entity.Entity entity) {
        final Entity minecraftEntity = ((CraftEntity) entity).getHandle();
        final String type = BuiltInRegistries.ENTITY_TYPE.getKey(minecraftEntity.getType()).toString();
        final String id = minecraftEntity.getStringUUID();
        if (minecraftEntity.getCustomName() == null) {
            return new net.md_5.bungee.api.chat.hover.content.Entity(type, id, null);
        }
        final BaseComponent[] array = ComponentSerializer.parse(Component.Serializer.toJson(minecraftEntity.getCustomName()));
        if (array.length == 1) {
            return new net.md_5.bungee.api.chat.hover.content.Entity(type, id, array[0]);
        }
        return new net.md_5.bungee.api.chat.hover.content.Entity(type, id, new TextComponent(array));
    }

    @Override
    public List<NamespacedKey> getLootTables() {
        ArrayList<NamespacedKey> lootTables = new ArrayList<>();
        ((CraftServer) Bukkit.getServer()).getServer().getLootData().getKeys(LootDataType.TABLE)
            .forEach(location -> lootTables.add(CraftNamespacedKey.fromMinecraft(location)));
        return lootTables;
    }

    @Override
    public org.bukkit.loot.LootTable getLootTable(NamespacedKey key) {
        LootTable table = ((CraftServer) Bukkit.getServer()).getServer().getLootData().getLootTable(CraftNamespacedKey.toMinecraft(key));
        if (table == LootTable.EMPTY) {
            return null;
        }
        return new CraftLootTable(key, table);
    }

    @Override
    public void fill(final Inventory inventory, final Player player, final Location location, final org.bukkit.loot.LootTable lootTable,
        final long seed) {
        final ServerPlayer minecraftPlayer = ((CraftPlayer) player).getHandle();
        final ServerLevel level = minecraftPlayer.serverLevel();
        if (level.getServer() == null) {
            return;
        }
        final LootTable table = level.getServer().getLootData().getLootTable(CraftNamespacedKey.toMinecraft(lootTable.getKey()));
        if (table == null) {
            return;
        }
        table.fill(((CraftInventory) inventory).getInventory(),
            new LootParams.Builder(minecraftPlayer.serverLevel())
                .withParameter(LootContextParams.ORIGIN,
                    Vec3.atCenterOf(new Vec3i(location.getBlockX(), location.getBlockY(), location.getBlockZ())))
                .withParameter(LootContextParams.THIS_ENTITY, minecraftPlayer).withLuck(minecraftPlayer.getLuck())
                .create(LootContextParamSets.CHEST),
            seed);
    }

}