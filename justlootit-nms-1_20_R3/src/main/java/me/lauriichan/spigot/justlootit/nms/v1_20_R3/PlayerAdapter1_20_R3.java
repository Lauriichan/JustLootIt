package me.lauriichan.spigot.justlootit.nms.v1_20_R3;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.netty.channel.Channel;
import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.laylib.reflection.JavaAccess;
import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.packet.AbstractPacketOut;
import me.lauriichan.spigot.justlootit.nms.v1_20_R3.network.PlayerNetwork1_20_R3;
import me.lauriichan.spigot.justlootit.nms.v1_20_R3.util.MinecraftConstant1_20_R3;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;

public final class PlayerAdapter1_20_R3 extends PlayerAdapter {

    private static final MethodHandle PlayerConnection_connection = JavaAccess
        .accessFieldGetter(ClassUtil.getField(ServerCommonPacketListenerImpl.class, false, Connection.class));

    private final VersionHandler1_20_R3 versionHandler;
    private final PlayerNetwork1_20_R3 network;

    private final CraftPlayer bukkit;
    private final ServerPlayer minecraft;

    public PlayerAdapter1_20_R3(final VersionHandler1_20_R3 versionHandler, final Player player) {
        super(player.getUniqueId());
        this.bukkit = (CraftPlayer) player;
        this.minecraft = bukkit.getHandle();
        this.network = new PlayerNetwork1_20_R3(versionHandler.packetManager(), this);
        this.versionHandler = versionHandler;
    }

    final void terminateAdapter() {
        network.setActive(false);
    }

    @Override
    public VersionHandler1_20_R3 versionHandler() {
        return versionHandler;
    }

    @Override
    public PlayerNetwork1_20_R3 getNetwork() {
        return network;
    }

    @Override
    public ServerPlayer asMinecraft() {
        return minecraft;
    }

    @Override
    public int createAnvilMenu(final String name, final ItemStack itemStack) {
        if (!Bukkit.isPrimaryThread()) {
            return CompletableFuture.supplyAsync(() -> createAnvilMenu(name, itemStack), network.packetManager().mainService()).join();
        }
        final AnvilMenu menu = new AnvilMenu(minecraft.nextContainerCounter(), minecraft.getInventory(),
            MinecraftConstant1_20_R3.BETTER_NULL);
        menu.getSlot(0).set(CraftItemStack.asNMSCopy(itemStack));
        menu.setTitle(Component.literal(name));
        minecraft.containerMenu = menu;
        minecraft.connection.send(new ClientboundOpenScreenPacket(menu.containerId, menu.getType(), menu.getTitle()));
        minecraft.initMenu(menu);
        return menu.containerId;
    }

    @Override
    public void reopenMenu() {
        final AbstractContainerMenu menu = minecraft.containerMenu;
        minecraft.connection.send(new ClientboundOpenScreenPacket(menu.containerId, menu.getType(), menu.getTitle()));
    }

    @Override
    public void closeMenu() {
        minecraft.connection.send(new ClientboundContainerClosePacket(minecraft.containerMenu.containerId));
        minecraft.doCloseContainer();
    }

    @Override
    public CraftPlayer asBukkit() {
        return bukkit;
    }

    @Override
    public LevelAdapter getLevel() {
        return versionHandler.getLevel(minecraft.level().getWorld());
    }

    @Override
    public int getPermissionLevel() {
        return minecraft.getServer().getProfilePermissions(minecraft.getGameProfile());
    }

    @Override
    public void send(final AbstractPacketOut... packets) {
        if (minecraft.connection.isDisconnected()) {
            return;
        }
        for (final AbstractPacketOut packet : packets) {
            if (!(packet.asMinecraft() instanceof Packet)) {
                continue;
            }
            minecraft.connection.send((Packet<?>) packet.asMinecraft());
        }
    }

    @Override
    public void acknowledgeBlockChangesUpTo(final int sequence) {
        minecraft.connection.ackBlockChangesUpTo(sequence);
    }

    public Channel getChannel() {
        Connection connection;
        try {
            connection = (Connection) PlayerConnection_connection.invoke(minecraft.connection);
        } catch (final Throwable e) {
            throw new IllegalStateException("Unable to get player channel", e);
        }
        return connection.channel;
    }

}