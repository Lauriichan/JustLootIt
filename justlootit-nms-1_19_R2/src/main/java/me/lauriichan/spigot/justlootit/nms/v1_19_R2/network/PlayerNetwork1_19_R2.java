package me.lauriichan.spigot.justlootit.nms.v1_19_R2.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import me.lauriichan.spigot.justlootit.nms.*;
import me.lauriichan.spigot.justlootit.nms.util.Ref;
import me.lauriichan.spigot.justlootit.nms.v1_19_R2.*;
import net.minecraft.network.protocol.Packet;

public class PlayerNetwork1_19_R2 implements IPlayerNetwork {

    private final PacketManager1_19_R2 packetManager;

    private final PacketInHandler1_19_R2 packetIn = new PacketInHandler1_19_R2(this);
    private final PacketOutHandler1_19_R2 packetOut = new PacketOutHandler1_19_R2(this);

    private final PlayerAdapter1_19_R2 player;

    private boolean active = true;

    public PlayerNetwork1_19_R2(final PacketManager1_19_R2 packetManager, final PlayerAdapter1_19_R2 player) {
        this.packetManager = packetManager;
        this.player = player;
        add(player.getChannel());
    }

    public PacketManager1_19_R2 packetManager() {
        return packetManager;
    }

    private void remove(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        if (pipeline.get("bi:in") != null) {
            pipeline.remove("bi:in");
        }
        if (pipeline.get("bi:out") != null) {
            pipeline.remove("bi:out");
        }
    }

    private void add(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        if (pipeline.get("bi:in") == null) {
            pipeline.addAfter("decoder", "bi:in", packetIn);
        }
        if (pipeline.get("bi:out") == null) {
            pipeline.addAfter("encoder", "bi:out", packetOut);
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        if (this.active == active) {
            return;
        }
        this.active = active;
        remove(player.getChannel());
        if (active) {
            add(player.getChannel());
            return;
        }
    }

    boolean call(Ref<Packet<?>> nmsPacket) {
        return packetManager.call(player, nmsPacket);
    }

}
