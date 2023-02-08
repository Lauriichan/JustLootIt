package me.lauriichan.spigot.justlootit.nms.packet.listener;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.packet.AbstractPacket;

public final class PacketExecutor {

    @FunctionalInterface
    private static interface MethodExecutor {

        Object execute(MethodHandle method, PacketContainer container, PlayerAdapter player, AbstractPacket packet)
            throws Throwable;

    }

    private static final MethodExecutor PACKET = (method, container, player, packet) -> method.invokeWithArguments(container.getInstance(),
        packet);
    private static final MethodExecutor PACKET_PLAYER = (method, container, player, packet) -> method
        .invokeWithArguments(container.getInstance(), packet, player);
    private static final MethodExecutor PACKET_CONTAINER = (method, container, player, packet) -> method
        .invokeWithArguments(container.getInstance(), packet, container);
    private static final MethodExecutor PACKET_CONTAINER_PLAYER = (method, container, player, packet) -> method
        .invokeWithArguments(container.getInstance(), packet, container, player);
    private static final MethodExecutor PACKET_PLAYER_CONTAINER = (method, container, player, packet) -> method
        .invokeWithArguments(container.getInstance(), packet, player, container);

    private static final MethodExecutor PLAYER_PACKET = (method, container, player, packet) -> method
        .invokeWithArguments(container.getInstance(), player, packet);
    private static final MethodExecutor PLAYER_CONTAINER_PACKET = (method, container, player, packet) -> method
        .invokeWithArguments(container.getInstance(), player, container, packet);
    private static final MethodExecutor PLAYER_PACKET_CONTAINER = (method, container, player, packet) -> method
        .invokeWithArguments(container.getInstance(), player, packet, container);

    private static final MethodExecutor CONTAINER_PACKET = (method, container, player, packet) -> method
        .invokeWithArguments(container.getInstance(), container, packet);
    private static final MethodExecutor CONTAINER_PACKET_PLAYER = (method, container, player, packet) -> method
        .invokeWithArguments(container.getInstance(), container, packet, player);
    private static final MethodExecutor CONTAINER_PLAYER_PACKET = (method, container, player, packet) -> method
        .invokeWithArguments(container.getInstance(), container, player, packet);

    private final Class<?> packetType;
    private final MethodHandle method;
    private final MethodExecutor executor;

    private final boolean allowCancelled;

    private PacketExecutor(final Class<?> packetType, final MethodHandle method, final MethodExecutor executor,
        final boolean allowCancelled) {
        this.packetType = packetType;
        this.method = method;
        this.executor = executor;
        this.allowCancelled = allowCancelled;
    }

    public boolean doesAllowCancelled() {
        return allowCancelled;
    }

    public Class<?> getPacketType() {
        return packetType;
    }

    public boolean execute(PacketContainer container, PlayerAdapter player, AbstractPacket packet) {
        try {
            return sanitize(executor.execute(method, container, player, packet));
        } catch (Throwable e) {
            // TODO: Log if debug enabled otherwise ignore
            return false; // Do not cancel the packet
        }
    }

    private final boolean sanitize(Object object) {
        if (object == null || !(object instanceof Boolean)) {
            return false;
        }
        return ((Boolean) object).booleanValue();
    }

    static PacketExecutor analyze(Method method, boolean allowCancelled) {
        if (method.getParameterCount() > 3 || method.getParameterCount() < 1) {
            return null;
        }
        MethodHandle methodHandle;
        try {
            methodHandle = MethodHandles.lookup().unreflect(method);
        } catch (IllegalAccessException e) {
            return null; // Couldn't retrieve MethodHandle
        }
        Parameter[] parameters = method.getParameters();
        switch (parameters.length) {
        case 2:
            Class<?> param20 = parameters[0].getType();
            Class<?> param21 = parameters[1].getType();
            if (AbstractPacket.class.isAssignableFrom(param20)) {
                if (PlayerAdapter.class.isAssignableFrom(param21)) {
                    return new PacketExecutor(param20, methodHandle, PACKET_PLAYER, allowCancelled);
                } else if (PacketContainer.class.isAssignableFrom(param21)) {
                    return new PacketExecutor(param20, methodHandle, PACKET_CONTAINER, allowCancelled);
                }
                return null; // Unsupported type
            }
            if (AbstractPacket.class.isAssignableFrom(param21)) {
                if (PlayerAdapter.class.isAssignableFrom(param20)) {
                    return new PacketExecutor(param21, methodHandle, PLAYER_PACKET, allowCancelled);
                } else if (PacketContainer.class.isAssignableFrom(param20)) {
                    return new PacketExecutor(param21, methodHandle, CONTAINER_PACKET, allowCancelled);
                }
                return null; // Unsupported type
            }
            return null; // Unsupported type
        case 3:
            Class<?> param30 = parameters[0].getType();
            Class<?> param31 = parameters[1].getType();
            Class<?> param32 = parameters[1].getType();
            if (AbstractPacket.class.isAssignableFrom(param30)) {
                if (PlayerAdapter.class.isAssignableFrom(param31)) {
                    if (PacketContainer.class.isAssignableFrom(param32)) {
                        return new PacketExecutor(param30, methodHandle, PACKET_PLAYER_CONTAINER, allowCancelled);
                    }
                    return null; // Unsupported type
                } else if (PacketContainer.class.isAssignableFrom(param31)) {
                    if (PlayerAdapter.class.isAssignableFrom(param32)) {
                        return new PacketExecutor(param30, methodHandle, PACKET_CONTAINER_PLAYER, allowCancelled);
                    }
                    return null; // Unsupported type
                }
                return null; // Unsupported type
            }
            if (AbstractPacket.class.isAssignableFrom(param31)) {
                if (PlayerAdapter.class.isAssignableFrom(param30)) {
                    if (PacketContainer.class.isAssignableFrom(param32)) {
                        return new PacketExecutor(param31, methodHandle, PLAYER_PACKET_CONTAINER, allowCancelled);
                    }
                    return null; // Unsupported type
                } else if (PacketContainer.class.isAssignableFrom(param30)) {
                    if (PlayerAdapter.class.isAssignableFrom(param32)) {
                        return new PacketExecutor(param31, methodHandle, CONTAINER_PACKET_PLAYER, allowCancelled);
                    }
                    return null; // Unsupported type
                }
                return null; // Unsupported type
            }
            if (AbstractPacket.class.isAssignableFrom(param32)) {
                if (PlayerAdapter.class.isAssignableFrom(param30)) {
                    if (PacketContainer.class.isAssignableFrom(param31)) {
                        return new PacketExecutor(param31, methodHandle, PLAYER_CONTAINER_PACKET, allowCancelled);
                    }
                    return null; // Unsupported type
                } else if (PacketContainer.class.isAssignableFrom(param30)) {
                    if (PlayerAdapter.class.isAssignableFrom(param31)) {
                        return new PacketExecutor(param31, methodHandle, CONTAINER_PLAYER_PACKET, allowCancelled);
                    }
                    return null; // Unsupported type
                }
                return null; // Unsupported type
            }
            return null; // Unsupported type
        default:
            Class<?> param10 = parameters[0].getType();
            if (AbstractPacket.class.isAssignableFrom(param10)) {
                return new PacketExecutor(param10, methodHandle, PACKET, allowCancelled);
            }
            return null; // Unsupported type
        }
    }

}
