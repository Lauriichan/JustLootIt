package me.lauriichan.spigot.justlootit.nms.paper.v26_2.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import me.lauriichan.spigot.justlootit.nms.IPlayerNetwork;
import me.lauriichan.spigot.justlootit.nms.paper.v26_2.PlayerAdapter26_2;
import me.lauriichan.spigot.justlootit.nms.util.Ref;
import net.minecraft.network.protocol.Packet;

public class PlayerNetwork26_2 implements IPlayerNetwork {

    private final PacketManager26_2 packetManager;

    private final PacketInHandler26_2 packetIn = new PacketInHandler26_2(this);
    private final PacketOutHandler26_2 packetOut = new PacketOutHandler26_2(this);

    private final PlayerAdapter26_2 player;

    private boolean active = true;

    public PlayerNetwork26_2(final PacketManager26_2 packetManager, final PlayerAdapter26_2 player) {
        this.packetManager = packetManager;
        this.player = player;
        add(player.getChannel());
    }

    public PacketManager26_2 packetManager() {
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