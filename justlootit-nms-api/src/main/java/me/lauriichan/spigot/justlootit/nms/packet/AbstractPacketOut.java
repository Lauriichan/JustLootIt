package me.lauriichan.spigot.justlootit.nms.packet;

public abstract class AbstractPacketOut extends AbstractPacket {

    @Override
    public final boolean isIncoming() {
        return false;
    }

    @Override
    public final boolean isOutgoing() {
        return true;
    }

}
