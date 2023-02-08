package me.lauriichan.spigot.justlootit.nms.v1_19_R2.network;

import io.netty.channel.ChannelHandler.Sharable;
import me.lauriichan.spigot.justlootit.nms.util.Ref;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.Packet;

@Sharable
public final class PacketOutHandler1_19_R2 extends ChannelOutboundHandlerAdapter {

    private final PlayerNetwork1_19_R2 network;

    public PacketOutHandler1_19_R2(final PlayerNetwork1_19_R2 network) {
        this.network = network;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof Packet)) {
            ctx.write(msg, promise);
            return;
        }
        Ref<Packet<?>> packetRef = Ref.of((Packet<?>) msg);
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
