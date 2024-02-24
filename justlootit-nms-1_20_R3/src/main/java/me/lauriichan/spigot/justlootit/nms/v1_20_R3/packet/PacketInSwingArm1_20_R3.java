package me.lauriichan.spigot.justlootit.nms.v1_20_R3.packet;

import me.lauriichan.spigot.justlootit.nms.packet.PacketInSwingArm;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;

public final class PacketInSwingArm1_20_R3 extends PacketInSwingArm {

    private final ServerboundSwingPacket packet;

    public PacketInSwingArm1_20_R3(final ServerboundSwingPacket packet) {
        this.packet = packet;
    }

    @Override
    public Object asMinecraft() {
        return packet;
    }

    @Override
    public Hand getHand() {
        return packet.getHand() == InteractionHand.MAIN_HAND ? Hand.MAIN_HAND : Hand.OFF_HAND;
    }

}