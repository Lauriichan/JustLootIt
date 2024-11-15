package me.lauriichan.spigot.justlootit.nms.v1_21_R2.network;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.lauriichan.spigot.justlootit.nms.util.Ref;
import net.minecraft.network.protocol.Packet;

@Sharable
public final class PacketInHandler1_21_R2 extends ChannelInboundHandlerAdapter {

    private final PlayerNetwork1_21_R2 network;

    public PacketInHandler1_21_R2(final PlayerNetwork1_21_R2 network) {
        this.network = network;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (!(msg instanceof Packet)) {
            ctx.fireChannelRead(msg);
            return;
        }
        final Ref<Packet<?>> packetRef = Ref.of((Packet<?>) msg);
        if (network.call(packetRef)) {
            return;
        }
        ctx.fireChannelRead(packetRef.get());
    }

}