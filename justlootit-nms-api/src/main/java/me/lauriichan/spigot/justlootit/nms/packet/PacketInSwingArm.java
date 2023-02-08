package me.lauriichan.spigot.justlootit.nms.packet;

public abstract class PacketInSwingArm extends AbstractPacketIn {

    public static enum Hand {
        
        OFF_HAND,
        MAIN_HAND;
        
    }
    
    public abstract Hand getHand();

}
