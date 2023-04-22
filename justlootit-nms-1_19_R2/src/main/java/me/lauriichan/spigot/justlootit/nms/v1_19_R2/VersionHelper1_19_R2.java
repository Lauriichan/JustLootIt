package me.lauriichan.spigot.justlootit.nms.v1_19_R2;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_19_R2.util.CraftNamespacedKey;
import org.bukkit.craftbukkit.v1_19_R2.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import me.lauriichan.spigot.justlootit.nms.VersionHelper;
import me.lauriichan.spigot.justlootit.nms.v1_19_R2.io.ItemStackIO1_19_R2;
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
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class VersionHelper1_19_R2 extends VersionHelper {

    private final VersionHandler1_19_R2 handler;

    public VersionHelper1_19_R2(final VersionHandler1_19_R2 handler) {
        this.handler = handler;
    }

    @Override
    public VersionHandler1_19_R2 handler() {
        return handler;
    }

    @Override
    public ItemTag asItemTag(final org.bukkit.inventory.ItemStack itemStack) {
        return ItemTag.ofNbt(ItemStackIO1_19_R2.ITEM_STACK.asNbt(itemStack).getAsString());
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
        ((CraftServer) Bukkit.getServer()).getServer().getLootTables().getIds()
            .forEach(location -> lootTables.add(CraftNamespacedKey.fromMinecraft(location)));
        return lootTables;
    }

    @Override
    public void fill(final Inventory inventory, final Player player, final Location location, final org.bukkit.loot.LootTable lootTable,
        final long seed) {
        final ServerPlayer minecraftPlayer = ((CraftPlayer) player).getHandle();
        final ServerLevel level = minecraftPlayer.getLevel();
        if (level.getServer() == null) {
            return;
        }
        final LootTable table = level.getServer().getLootTables().get(CraftNamespacedKey.toMinecraft(lootTable.getKey()));
        if (table == null) {
            return;
        }
        table.fill(((CraftInventory) inventory).getInventory(),
            new LootContext.Builder(minecraftPlayer.getLevel())
                .withParameter(LootContextParams.ORIGIN,
                    Vec3.atCenterOf(new Vec3i(location.getBlockX(), location.getBlockY(), location.getBlockZ())))
                .withOptionalRandomSeed(seed).withLuck(minecraftPlayer.getLuck())
                .withParameter(LootContextParams.THIS_ENTITY, minecraftPlayer).create(LootContextParamSets.CHEST));
    }

}
