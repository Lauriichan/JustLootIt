package me.lauriichan.spigot.justlootit.nms.v1_21_R4;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_21_R4.CraftLootTable;
import org.bukkit.craftbukkit.v1_21_R4.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_21_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R4.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_21_R4.util.CraftNamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.VersionHelper;
import me.lauriichan.spigot.justlootit.nms.v1_21_R4.util.NmsHelper1_21_R4;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.core.Vec3i;
import net.minecraft.core.RegistryAccess.Frozen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class VersionHelper1_21_R4 extends VersionHelper {

    private final VersionHandler1_21_R4 handler;
    
    private final Frozen registry;
    private final ReloadableServerRegistries.Holder resourceRegistry;

    public VersionHelper1_21_R4(final VersionHandler1_21_R4 handler) {
        this.handler = handler;
        MinecraftServer server = NmsHelper1_21_R4.getServer();
        this.registry = server.registryAccess();
        this.resourceRegistry = server.resources.managers().fullRegistries();
    }

    @Override
    public VersionHandler handler() {
        return handler;
    }

    @Override
    public net.md_5.bungee.api.chat.hover.content.Entity createEntityHover(final org.bukkit.entity.Entity entity) {
        final Entity minecraftEntity = ((CraftEntity) entity).getHandle();
        final String type = BuiltInRegistries.ENTITY_TYPE.getKey(minecraftEntity.getType()).toString();
        final String id = minecraftEntity.getStringUUID();
        if (minecraftEntity.getCustomName() == null) {
            return new net.md_5.bungee.api.chat.hover.content.Entity(type, id, null);
        }
        final BaseComponent[] array = ComponentSerializer.parse(Component.Serializer.toJson(minecraftEntity.getCustomName(), registry));
        if (array.length == 1) {
            return new net.md_5.bungee.api.chat.hover.content.Entity(type, id, array[0]);
        }
        return new net.md_5.bungee.api.chat.hover.content.Entity(type, id, new TextComponent(array));
    }

    @Override
    public List<NamespacedKey> getLootTables() {
        ArrayList<NamespacedKey> lootTables = new ArrayList<>();
        resourceRegistry.getKeys(Registries.LOOT_TABLE)
            .forEach(location -> lootTables.add(CraftNamespacedKey.fromMinecraft(location)));
        return lootTables;
    }

    @Override
    public org.bukkit.loot.LootTable getLootTable(NamespacedKey key) {
        LootTable table = resourceRegistry.getLootTable(ResourceKey.create(Registries.LOOT_TABLE, CraftNamespacedKey.toMinecraft(key)));
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
        final LootTable table = resourceRegistry.getLootTable(ResourceKey.create(Registries.LOOT_TABLE, CraftNamespacedKey.toMinecraft(lootTable.getKey())));
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