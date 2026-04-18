package me.lauriichan.spigot.justlootit.nms.paper.v26_1.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import me.lauriichan.spigot.justlootit.nms.IPlayerNetwork;
import me.lauriichan.spigot.justlootit.nms.paper.v26_1.PlayerAdapter26_1;
import me.lauriichan.spigot.justlootit.nms.util.Ref;
import net.minecraft.network.protocol.Packet;

public class PlayerNetwork26_1 implements IPlayerNetwork {

    private final PacketManager26_1 packetManager;

    private final PacketInHandler26_1 packetIn = new PacketInHandler26_1(this);
    private final PacketOutHandler26_1 packetOut = new PacketOutHandler26_1(this);

    private final PlayerAdapter26_1 player;

    private boolean active = true;

    public PlayerNetwork26_1(final PacketManager26_1 packetManager, final PlayerAdapter26_1 player) {
        this.packetManager = packetManager;
        this.player = player;
        add(player.getChannel());
    }

    public PacketManager26_1 packetManager() {
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