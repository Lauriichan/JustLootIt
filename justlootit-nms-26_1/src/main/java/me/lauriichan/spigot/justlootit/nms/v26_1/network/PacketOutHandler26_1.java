package me.lauriichan.spigot.justlootit.nms.v26_1.network;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import me.lauriichan.spigot.justlootit.nms.util.Ref;
import net.minecraft.network.protocol.Packet;

@Sharable
public final class PacketOutHandler26_1 extends ChannelOutboundHandlerAdapter {

    private final PlayerNetwork26_1 network;

    public PacketOutHandler26_1(final PlayerNetwork26_1 network) {
        this.network = network;
    }

    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) throws Exception {
        if (!(msg instanceof Packet)) {
            ctx.write(msg, promise);
            return;
        }
        final Ref<Packet<?>> packetRef = Ref.of((Packet<?>) msg);
        if (network.call(packetRef)) {
            if (promise == null) {
                return;
            }
            promise.cancel(true);
            return;
        }
        ctx.write(packetRef.get(), promise);
    }
}