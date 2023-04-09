package me.lauriichan.spigot.justlootit.nms.v1_19_R3.packet;

import org.bukkit.Location;

import me.lauriichan.spigot.justlootit.nms.packet.PacketInUseItemOn;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;

public final class PacketInUseItemOn1_19_R3 extends PacketInUseItemOn {

    private final ServerboundUseItemOnPacket packet;

    public PacketInUseItemOn1_19_R3(final ServerboundUseItemOnPacket packet) {
        this.packet = packet;
    }

    @Override
    public Object asMinecraft() {
        return packet;
    }

    @Override
    public Location getHitLocation() {
        BlockPos pos = packet.getHitResult().getBlockPos();
        return new Location(null, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public int getSequence() {
        return packet.getSequence();
    }

}