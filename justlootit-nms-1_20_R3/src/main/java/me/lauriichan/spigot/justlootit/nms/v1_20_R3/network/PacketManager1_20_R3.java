package me.lauriichan.spigot.justlootit.nms.v1_20_R3.network;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.spigot.justlootit.nms.packet.AbstractPacket;
import me.lauriichan.spigot.justlootit.nms.packet.AbstractPacketOut;
import me.lauriichan.spigot.justlootit.nms.packet.listener.PacketManager;
import me.lauriichan.spigot.justlootit.nms.util.Ref;
import me.lauriichan.spigot.justlootit.nms.util.argument.ArgumentMap;
import me.lauriichan.spigot.justlootit.nms.util.argument.NotEnoughArgumentsException;
import me.lauriichan.spigot.justlootit.nms.v1_20_R3.PlayerAdapter1_20_R3;
import me.lauriichan.spigot.justlootit.nms.v1_20_R3.VersionHandler1_20_R3;
import net.minecraft.network.protocol.Packet;

public final class PacketManager1_20_R3 extends PacketManager {

    private static class NmsPacketBuilder<P extends Packet<?>> {

        private final Function<P, AbstractPacket> creator;
        private final Class<P> packetType;

        NmsPacketBuilder(final Class<P> packetType, final Function<P, AbstractPacket> creator) {
            this.packetType = packetType;
            this.creator = creator;
        }

        // We are sure that this is the correct packet type
        AbstractPacket build(final Packet<?> packet) {
            return creator.apply(packetType.cast(packet));
        }

    }

    private Map<Class<?>, NmsPacketBuilder<?>> nmsBuilders = new HashMap<>();
    private Map<Class<?>, Function<ArgumentMap, ? extends AbstractPacketOut>> adapterBuilders = new HashMap<>();

    public PacketManager1_20_R3(final VersionHandler1_20_R3 handler) {
        super(handler);
    }

    public <P extends Packet<?>> void register(final Class<P> packetType, final Function<P, AbstractPacket> function) {
        if (!(nmsBuilders instanceof HashMap)) {
            return;
        }
        Objects.requireNonNull(packetType);
        Objects.requireNonNull(function);
        if (nmsBuilders.containsKey(packetType)) {
            return;
        }
        nmsBuilders.put(packetType, new NmsPacketBuilder<>(packetType, function));
    }

    public <P extends AbstractPacketOut> void registerAdapter(final Class<P> packetType,
        final Function<ArgumentMap, ? extends P> function) {
        if (!(adapterBuilders instanceof HashMap)) {
            return;
        }
        Objects.requireNonNull(packetType);
        Objects.requireNonNull(function);
        if (adapterBuilders.containsKey(packetType)) {
            return;
        }
        adapterBuilders.put(packetType, function);
    }

    boolean call(final PlayerAdapter1_20_R3 player, final Ref<Packet<?>> nmsPacket) {
        final AbstractPacket packet;
        try {
            final NmsPacketBuilder<?> builder = nmsBuilders.get(nmsPacket.get().getClass());
            if (builder == null) {
                return false;
            }
            packet = builder.build(nmsPacket.get());
        } catch (Throwable thrw) {
            logger().error(thrw);
            return false;
        }
        if (packet == null) {
            return false;
        }
        try {
            return call(player, packet);
        } finally {
            final Packet<?> minecraftPacket = (Packet<?>) packet.asMinecraft();
            if (minecraftPacket != nmsPacket.get()) {
                nmsPacket.set(minecraftPacket);
            }
        }
    }

    @Override
    public <P extends AbstractPacketOut> P createPacket(final ArgumentMap map, final Class<P> packetType)
        throws NotEnoughArgumentsException, IllegalStateException, IllegalArgumentException {
        final Function<ArgumentMap, ? extends AbstractPacket> function = adapterBuilders.get(packetType);
        if (function == null) {
            return null;
        }
        final AbstractPacket packet = function.apply(map);
        if (packet == null || !packetType.isAssignableFrom(packet.getClass())) {
            throw new IllegalStateException("Invalid packet of type '" + ClassUtil.getClassName(packetType) + "'!");
        }
        return packetType.cast(packet);
    }

    public void finish() {
        if (!(nmsBuilders instanceof HashMap)) {
            return;
        }
        this.nmsBuilders = Collections.unmodifiableMap(nmsBuilders);
        this.adapterBuilders = Collections.unmodifiableMap(adapterBuilders);
    }

}