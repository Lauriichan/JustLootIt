package me.lauriichan.spigot.justlootit.nms.v1_19_R2;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.bukkit.craftbukkit.v1_19_R2.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R2.util.CraftMagicNumbers;

import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.laylib.reflection.JavaAccess;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.VersionHelper;
import me.lauriichan.spigot.justlootit.nms.model.IEntityData;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;

public class VersionHelper1_19_R2 extends VersionHelper {

    private final Method CraftEntityState_getTileEntity = ClassUtil.getMethod(CraftBlockEntityState.class, "getTileEntity");
    private final Field CraftItemStack_handle = ClassUtil.getField(CraftItemStack.class, "handle");

    private final VersionHandler1_19_R2 handler;

    public VersionHelper1_19_R2(final VersionHandler1_19_R2 handler) {
        this.handler = handler;
    }

    @Override
    public VersionHandler handler() {
        return handler;
    }

    @Override
    protected void fillLoot(PlayerAdapter player, org.bukkit.block.BlockState state) {
        ((RandomizableContainerBlockEntity) JavaAccess.invoke(state, CraftEntityState_getTileEntity))
            .unpackLootTable((ServerPlayer) player.asMinecraft());
    }

    @Override
    protected void fillLoot(PlayerAdapter player, org.bukkit.entity.Minecart entity) {
        ((AbstractMinecartContainer) ((CraftEntity) entity).getHandle()).unpackChestVehicleLootTable((ServerPlayer) player.asMinecraft());
    }

    @Override
    public ItemTag asItemTag(org.bukkit.inventory.ItemStack itemStack) {
        return ItemTag.ofNbt(extract(itemStack).getOrCreateTag().getAsString());
    }

    private ItemStack extract(org.bukkit.inventory.ItemStack itemStack) {
        if (itemStack instanceof CraftItemStack) {
            Object handle = JavaAccess.getObjectValue(itemStack, CraftItemStack_handle);
            if (handle == null) {
                return ItemStack.EMPTY;
            }
            return (ItemStack) handle;
        }
        if (itemStack == null || itemStack.getType() == org.bukkit.Material.AIR) {
            return ItemStack.EMPTY;
        }
        @SuppressWarnings("deprecation")
        Item item = CraftMagicNumbers.getItem(itemStack.getType(), itemStack.getDurability());
        if (item == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item, itemStack.getAmount());
        if (itemStack.hasItemMeta()) {
            CraftItemStack.setItemMeta(stack, itemStack.getItemMeta());
        }
        return stack;
    }

    @Override
    public net.md_5.bungee.api.chat.hover.content.Entity createEntityHover(org.bukkit.entity.Entity entity) {
        Entity minecraftEntity = ((CraftEntity) entity).getHandle();
        String type = BuiltInRegistries.ENTITY_TYPE.getKey(minecraftEntity.getType()).toString();
        String id = minecraftEntity.getStringUUID();
        if (minecraftEntity.getCustomName() == null) {
            return new net.md_5.bungee.api.chat.hover.content.Entity(type, id, null);
        }
        BaseComponent[] array = ComponentSerializer.parse(Component.Serializer.toJson(minecraftEntity.getCustomName()));
        if (array.length == 1) {
            return new net.md_5.bungee.api.chat.hover.content.Entity(type, id, array[0]);
        }
        return new net.md_5.bungee.api.chat.hover.content.Entity(type, id, new TextComponent(array));
    }

    @Override
    public List<IEntityData> getEntityData(org.bukkit.entity.Entity entity) {
        // TODO: Possibly implement this, I don't know if we even need this so I'll just leave it here for now.
        //       If it is required this should be implemented otherwise it should be removed before release.
        return null;
    }

}
