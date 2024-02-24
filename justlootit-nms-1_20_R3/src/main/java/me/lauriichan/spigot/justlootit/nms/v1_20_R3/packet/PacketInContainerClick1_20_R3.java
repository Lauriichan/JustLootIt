package me.lauriichan.spigot.justlootit.nms.v1_20_R3.packet;

import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import me.lauriichan.spigot.justlootit.nms.packet.PacketInContainerClick;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;

public final class PacketInContainerClick1_20_R3 extends PacketInContainerClick {

    private final ServerboundContainerClickPacket packet;

    public PacketInContainerClick1_20_R3(final ServerboundContainerClickPacket packet) {
        this.packet = packet;
    }

    @Override
    public int getContainerId() {
        return packet.getContainerId();
    }

    @Override
    public int getSlot() {
        return packet.getSlotNum();
    }

    @Override
    public ClickAction getAction() {
        switch (packet.getClickType()) {
        case CLONE:
            return ClickAction.CLONE;
        case PICKUP_ALL:
            return ClickAction.PICKUP_ALL;
        case QUICK_CRAFT:
            return ClickAction.QUICK_CRAFT;
        case QUICK_MOVE:
            return ClickAction.QUICK_MOVE;
        case SWAP:
            return ClickAction.SWAP;
        case THROW:
            return ClickAction.THROW;
        case PICKUP:
        default:
            return ClickAction.PICKUP;
        }
    }

    @Override
    public ItemStack getItemStack() {
        return CraftItemStack.asCraftMirror(packet.getCarriedItem());
    }

    @Override
    public Object asMinecraft() {
        return packet;
    }

}