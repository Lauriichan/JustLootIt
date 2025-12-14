package me.lauriichan.spigot.justlootit.nms.v1_21_R7.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import me.lauriichan.spigot.justlootit.nms.IPlayerNetwork;
import me.lauriichan.spigot.justlootit.nms.util.Ref;
import me.lauriichan.spigot.justlootit.nms.v1_21_R7.PlayerAdapter1_21_R7;
import net.minecraft.network.protocol.Packet;

public class PlayerNetwork1_21_R7 implements IPlayerNetwork {

    private final PacketManager1_21_R7 packetManager;

    private final PacketInHandler1_21_R7 packetIn = new PacketInHandler1_21_R7(this);
    private final PacketOutHandler1_21_R7 packetOut = new PacketOutHandler1_21_R7(this);

    private final PlayerAdapter1_21_R7 player;

    private boolean active = true;

    public PlayerNetwork1_21_R7(final PacketManager1_21_R7 packetManager, final PlayerAdapter1_21_R7 player) {
        this.packetManager = packetManager;
        this.player = player;
        add(player.getChannel());
    }

    public PacketManager1_21_R7 packetManager() {
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