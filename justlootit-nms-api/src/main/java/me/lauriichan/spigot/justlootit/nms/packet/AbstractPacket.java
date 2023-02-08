package me.lauriichan.spigot.justlootit.nms.packet;

public abstract class AbstractPacket {

    public abstract Object asMinecraft();

    public abstract boolean isOutgoing();

    public abstract boolean isIncoming();

}
