package me.lauriichan.spigot.justlootit.nms.packet;

public abstract class AbstractPacketIn extends AbstractPacket {

    @Override
    public final boolean isIncoming() {
        return true;
    }

    @Override
    public final boolean isOutgoing() {
        return false;
    }

}
