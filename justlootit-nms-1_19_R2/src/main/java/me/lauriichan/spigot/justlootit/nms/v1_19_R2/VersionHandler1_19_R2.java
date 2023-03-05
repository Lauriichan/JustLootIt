package me.lauriichan.spigot.justlootit.nms.v1_19_R2;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import me.lauriichan.spigot.justlootit.nms.*;
import me.lauriichan.spigot.justlootit.nms.packet.*;
import me.lauriichan.spigot.justlootit.nms.v1_19_R2.io.*;
import me.lauriichan.spigot.justlootit.nms.v1_19_R2.network.*;
import me.lauriichan.spigot.justlootit.nms.v1_19_R2.packet.*;

import net.minecraft.network.protocol.game.*;

public final class VersionHandler1_19_R2 extends VersionHandler implements IServiceProvider {

    private final PacketManager1_19_R2 packetManager;
    private final VersionHelper1_19_R2 versionHelper;

    public VersionHandler1_19_R2(IServiceProvider provider) {
        super(provider);
        this.packetManager = new PacketManager1_19_R2(this);
        this.versionHelper = new VersionHelper1_19_R2(this);
    }

    @Override
    protected void onEnable(PluginManager pluginManager) {
        registerPackets();
        packetManager.finish();
        registerIO();
    }

    private void registerPackets() {
        // Incoming packets (nms)
        packetManager.register(ServerboundUseItemOnPacket.class, PacketInUseItemOn1_19_R2::new);
        packetManager.register(ServerboundSwingPacket.class, PacketInSwingArm1_19_R2::new);
        packetManager.register(ServerboundContainerClickPacket.class, PacketInContainerClick1_19_R2::new);
        // Outgoing packets (nms)
        packetManager.register(ClientboundSetEntityDataPacket.class, PacketOutSetEntityData1_19_R2::new);
        // Outgoing packets (adapter)
        packetManager.registerAdapter(PacketOutSetEntityData.class, PacketOutSetEntityData1_19_R2::new);
    }
    
    private void registerIO() {
        io.register(ItemStackIO1_19_R2.ITEM_STACK);
    }

    @Override
    public PacketManager1_19_R2 getPacketManager() {
        return packetManager;
    }

    @Override
    public VersionHelper1_19_R2 getVersionHelper() {
        return versionHelper;
    }
    
    @Override
    protected PlayerAdapter createAdapter(Player player) {
        return new PlayerAdapter1_19_R2(this, player);
    }

    @Override
    protected void terminateAdapter(PlayerAdapter adapter) {
        if (!(adapter instanceof PlayerAdapter1_19_R2)) {
            return;
        }
        ((PlayerAdapter1_19_R2) adapter).terminate();
    }

    @Override
    protected LevelAdapter createAdapter(World world) {
        if(!(world instanceof CraftWorld)) {
            return null;
        }
        return new LevelAdapter1_19_R2(((CraftWorld) world).getHandle());
    }

    @Override
    protected void terminateAdapter(LevelAdapter adapter) {}

}
