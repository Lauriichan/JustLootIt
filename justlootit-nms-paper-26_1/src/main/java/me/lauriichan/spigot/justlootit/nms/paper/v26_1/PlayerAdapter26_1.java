package me.lauriichan.spigot.justlootit.nms.paper.v26_1;

import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.netty.channel.Channel;
import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.packet.AbstractPacketOut;
import me.lauriichan.spigot.justlootit.nms.paper.v26_1.network.PlayerNetwork26_1;
import me.lauriichan.spigot.justlootit.nms.paper.v26_1.util.MinecraftConstant26_1;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;

public final class PlayerAdapter26_1 extends PlayerAdapter {

    private final VersionHandler26_1 versionHandler;
    private final PlayerNetwork26_1 network;

    private final CraftPlayer bukkit;
    private final ServerPlayer minecraft;

    public PlayerAdapter26_1(final VersionHandler26_1 versionHandler, final Player player) {
        super(player.getUniqueId());
        this.bukkit = (CraftPlayer) player;
        this.minecraft = bukkit.getHandle();
        this.network = new PlayerNetwork26_1(versionHandler.packetManager(), this);
        this.versionHandler = versionHandler;
    }

    final void terminateAdapter() {
        network.setActive(false);
    }

    @Override
    public VersionHandler26_1 versionHandler() {
        return versionHandler;
    }

    @Override
    public PlayerNetwork26_1 getNetwork() {
        return network;
    }

    @Override
    public ServerPlayer asMinecraft() {
        return minecraft;
    }

    @Override
    public int createAnvilMenu(final String name, final ItemStack itemStack) {
        if (!Bukkit.isPrimaryThread()) {
            return CompletableFuture.supplyAsync(() -> createAnvilMenu(name, itemStack), network.packetManager().scheduler().syncExecutor())
                .join();
        }
        final AnvilMenu menu = new AnvilMenu(minecraft.nextContainerCounter(), minecraft.getInventory(),
            MinecraftConstant26_1.BETTER_NULL);
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
        return minecraft.level().getServer().getProfilePermissions(minecraft.nameAndId()).level().id();
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
        return minecraft.connection.connection.channel;
    }

}