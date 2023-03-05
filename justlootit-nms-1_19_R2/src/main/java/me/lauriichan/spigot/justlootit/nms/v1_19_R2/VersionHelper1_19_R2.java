package me.lauriichan.spigot.justlootit.nms.v1_19_R2;

import org.bukkit.craftbukkit.v1_19_R2.entity.CraftEntity;

import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.VersionHelper;
import me.lauriichan.spigot.justlootit.nms.v1_19_R2.io.ItemStackIO1_19_R2;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class VersionHelper1_19_R2 extends VersionHelper {
    private final VersionHandler1_19_R2 handler;

    public VersionHelper1_19_R2(final VersionHandler1_19_R2 handler) {
        this.handler = handler;
    }

    @Override
    public VersionHandler handler() {
        return handler;
    }

    @Override
    public ItemTag asItemTag(org.bukkit.inventory.ItemStack itemStack) {
        return ItemTag.ofNbt(ItemStackIO1_19_R2.ITEM_STACK.asNbt(itemStack).getAsString());
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

}
