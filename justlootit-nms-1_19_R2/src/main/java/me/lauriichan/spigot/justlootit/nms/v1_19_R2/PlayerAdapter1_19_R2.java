package me.lauriichan.spigot.justlootit.nms.v1_19_R2;

import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.netty.channel.Channel;
import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.packet.AbstractPacketOut;
import me.lauriichan.spigot.justlootit.nms.v1_19_R2.network.*;
import me.lauriichan.spigot.justlootit.nms.v1_19_R2.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;

public final class PlayerAdapter1_19_R2 extends PlayerAdapter {

    private final VersionHandler1_19_R2 versionHandler;
    private final PlayerNetwork1_19_R2 network;

    private final CraftPlayer bukkit;
    private final ServerPlayer minecraft;

    public PlayerAdapter1_19_R2(VersionHandler1_19_R2 versionHandler, Player player) {
        super(player.getUniqueId());
        this.bukkit = (CraftPlayer) player;
        this.minecraft = bukkit.getHandle();
        this.network = new PlayerNetwork1_19_R2(versionHandler.packetManager(), this);
        this.versionHandler = versionHandler;
    }

    final void terminate() {
        network.setActive(false);
    }
    
    @Override
    public VersionHandler1_19_R2 versionHandler() {
        return versionHandler;
    }

    @Override
    public PlayerNetwork1_19_R2 getNetwork() {
        return network;
    }

    @Override
    public ServerPlayer asMinecraft() {
        return minecraft;
    }

    @Override
    public int createAnvilMenu(String name, ItemStack itemStack) {
        if(!Bukkit.isPrimaryThread()) {
            return CompletableFuture.supplyAsync(() -> createAnvilMenu(name, itemStack), network.packetManager().mainService()).join();
        }
        AnvilMenu menu = new AnvilMenu(minecraft.nextContainerCounter(), minecraft.getInventory(), MinecraftConstant1_19_R2.BETTER_NULL);
        menu.getSlot(0).set(CraftItemStack.asNMSCopy(itemStack));
        menu.setTitle(Component.literal(name));
        minecraft.containerMenu = menu;
        minecraft.connection.send(new ClientboundOpenScreenPacket(menu.containerId, menu.getType(), menu.getTitle()));
        minecraft.initMenu(menu);
        return menu.containerId;
    }

    @Override
    public void reopenMenu() {
        AbstractContainerMenu menu = minecraft.containerMenu;
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
        return versionHandler.getLevel(minecraft.getLevel().getWorld());
    }

    @Override
    public int getPermissionLevel() {
        return minecraft.getServer().getProfilePermissions(minecraft.getGameProfile());
    }

    @Override
    public void send(AbstractPacketOut... packets) {
        if (minecraft.connection.isDisconnected()) {
            return;
        }
        for (AbstractPacketOut packet : packets) {
            if (!(packet.asMinecraft() instanceof Packet)) {
                continue;
            }
            minecraft.connection.send((Packet<?>) packet.asMinecraft());
        }
    }

    @Override
    public void acknowledgeBlockChangesUpTo(int sequence) {
        minecraft.connection.ackBlockChangesUpTo(sequence);
    }

    public Channel getChannel() {
        return minecraft.connection.getConnection().channel;
    }

}
