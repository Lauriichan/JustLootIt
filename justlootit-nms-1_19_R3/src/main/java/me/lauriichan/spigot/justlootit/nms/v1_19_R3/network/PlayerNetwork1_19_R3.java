package me.lauriichan.spigot.justlootit.nms.v1_19_R3.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import me.lauriichan.spigot.justlootit.nms.IPlayerNetwork;
import me.lauriichan.spigot.justlootit.nms.util.Ref;
import me.lauriichan.spigot.justlootit.nms.v1_19_R3.PlayerAdapter1_19_R3;
import net.minecraft.network.protocol.Packet;

public class PlayerNetwork1_19_R3 implements IPlayerNetwork {

    private final PacketManager1_19_R3 packetManager;

    private final PacketInHandler1_19_R3 packetIn = new PacketInHandler1_19_R3(this);
    private final PacketOutHandler1_19_R3 packetOut = new PacketOutHandler1_19_R3(this);

    private final PlayerAdapter1_19_R3 player;

    private boolean active = true;

    public PlayerNetwork1_19_R3(final PacketManager1_19_R3 packetManager, final PlayerAdapter1_19_R3 player) {
        this.packetManager = packetManager;
        this.player = player;
        add(player.getChannel());
    }

    public PacketManager1_19_R3 packetManager() {
        return packetManager;
    }

    private void remove(final Channel channel) {
        final ChannelPipeline pipeline = channel.pipeline();
        if (pipeline.get(DECODER) != null) {
            pipeline.remove(DECODER);
        }
        if (pipeline.get(ENCODER) != null) {
            pipeline.remove(ENCODER);
        }
    }

    private void add(final Channel channel) {
        final ChannelPipeline pipeline = channel.pipeline();
        if (pipeline.get(ENCODER) == null) {
            pipeline.addAfter("decoder", ENCODER, packetIn);
        }
        if (pipeline.get(DECODER) == null) {
            pipeline.addAfter("encoder", DECODER, packetOut);
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(final boolean active) {
        if (this.active == active) {
            return;
        }
        this.active = active;
        remove(player.getChannel());
        if (active) {
            add(player.getChannel());
        }
    }

    boolean call(final Ref<Packet<?>> nmsPacket) {
        return packetManager.call(player, nmsPacket);
    }

}