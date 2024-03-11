package me.lauriichan.spigot.justlootit.nms.v1_20_R2;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import me.lauriichan.spigot.justlootit.nms.IServiceProvider;
import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.packet.PacketOutSetEntityData;
import me.lauriichan.spigot.justlootit.nms.v1_20_R2.debug.Debug1_20_R2;
import me.lauriichan.spigot.justlootit.nms.v1_20_R2.io.ItemStackIO1_20_R2;
import me.lauriichan.spigot.justlootit.nms.v1_20_R2.network.PacketManager1_20_R2;
import me.lauriichan.spigot.justlootit.nms.v1_20_R2.packet.*;
import net.minecraft.network.protocol.game.*;

public final class VersionHandler1_20_R2 extends VersionHandler {

    private final PacketManager1_20_R2 packetManager;
    private final VersionHelper1_20_R2 versionHelper;

    public VersionHandler1_20_R2(final IServiceProvider provider) {
        super(provider, new Debug1_20_R2());
        this.packetManager = new PacketManager1_20_R2(this);
        this.versionHelper = new VersionHelper1_20_R2(this);
    }

    @Override
    protected void onEnable(final PluginManager pluginManager) {
        registerPackets();
        packetManager.finish();
        registerIO();
    }

    private void registerPackets() {
        // Incoming packets (nms)
        // Outgoing packets (nms)
        packetManager.register(ClientboundAddEntityPacket.class, PacketOutAddEntity1_20_R2::new);
        packetManager.register(ClientboundSetEntityDataPacket.class, PacketOutSetEntityData1_20_R2::new);
        // Outgoing packets (adapter)
        packetManager.registerAdapter(PacketOutSetEntityData.class, PacketOutSetEntityData1_20_R2::new);
    }

    private void registerIO() {
        io.register(ItemStackIO1_20_R2.ITEM_STACK);
    }

    @Override
    public PacketManager1_20_R2 packetManager() {
        return packetManager;
    }

    @Override
    public VersionHelper1_20_R2 versionHelper() {
        return versionHelper;
    }

    @Override
    protected PlayerAdapter createAdapter(final Player player) {
        return new PlayerAdapter1_20_R2(this, player);
    }

    @Override
    protected void terminateAdapter(final PlayerAdapter adapter) {
        if (!(adapter instanceof PlayerAdapter1_20_R2)) {
            return;
        }
        ((PlayerAdapter1_20_R2) adapter).terminateAdapter();
    }

    @Override
    protected LevelAdapter createAdapter(final World world) {
        if (!(world instanceof CraftWorld)) {
            return null;
        }
        return new LevelAdapter1_20_R2(this, ((CraftWorld) world).getHandle());
    }

    @Override
    protected void terminateAdapter(final LevelAdapter adapter) {}

}