package me.lauriichan.spigot.justlootit.nms.v1_19_R3.network;

import io.netty.channel.ChannelHandler.Sharable;
import me.lauriichan.spigot.justlootit.nms.util.Ref;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.network.protocol.Packet;

@Sharable
public final class PacketInHandler1_19_R3 extends ChannelInboundHandlerAdapter {

    private final PlayerNetwork1_19_R3 network;

    public PacketInHandler1_19_R3(final PlayerNetwork1_19_R3 network) {
        this.network = network;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof Packet)) {
            ctx.fireChannelRead(msg);
            return;
        }
        Ref<Packet<?>> packetRef = Ref.of((Packet<?>) msg);
        if (network.call(packetRef)) {
            return;
        }
        ctx.fireChannelRead(packetRef.get());
    }

}