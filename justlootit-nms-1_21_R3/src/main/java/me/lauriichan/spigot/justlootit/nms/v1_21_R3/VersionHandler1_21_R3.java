package me.lauriichan.spigot.justlootit.nms.v1_21_R3;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import me.lauriichan.spigot.justlootit.nms.IServiceProvider;
import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.packet.PacketOutSetEntityData;
import me.lauriichan.spigot.justlootit.nms.v1_21_R3.convert.ConversionAdapter1_21_R3;
import me.lauriichan.spigot.justlootit.nms.v1_21_R3.debug.Debug1_21_R3;
import me.lauriichan.spigot.justlootit.nms.v1_21_R3.io.ItemStackIO1_21_R3;
import me.lauriichan.spigot.justlootit.nms.v1_21_R3.nbt.NbtHelper1_21_R3;
import me.lauriichan.spigot.justlootit.nms.v1_21_R3.network.PacketManager1_21_R3;
import me.lauriichan.spigot.justlootit.nms.v1_21_R3.packet.*;
import net.minecraft.network.protocol.game.*;

public final class VersionHandler1_21_R3 extends VersionHandler {
    
    private final NbtHelper1_21_R3 nbtHelper = new NbtHelper1_21_R3();

    private final PacketManager1_21_R3 packetManager;
    private final VersionHelper1_21_R3 versionHelper;
    
    private final ConversionAdapter1_21_R3 conversionAdapter;

    public VersionHandler1_21_R3(final IServiceProvider provider) {
        super(provider, new Debug1_21_R3());
        this.packetManager = new PacketManager1_21_R3(this);
        this.versionHelper = new VersionHelper1_21_R3(this);
        this.conversionAdapter = new ConversionAdapter1_21_R3(this);
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
        packetManager.register(ClientboundAddEntityPacket.class, PacketOutAddEntity1_21_R3::new);
        packetManager.register(ClientboundSetEntityDataPacket.class, PacketOutSetEntityData1_21_R3::new);
        // Outgoing packets (adapter)
        packetManager.registerAdapter(PacketOutSetEntityData.class, PacketOutSetEntityData1_21_R3::new);
    }

    private void registerIO() {
        io.register(ItemStackIO1_21_R3.ITEM_STACK);
    }
    
    @Override
    public NbtHelper1_21_R3 nbtHelper() {
        return nbtHelper;
    }

    @Override
    public ConversionAdapter1_21_R3 conversionAdapter() {
        return conversionAdapter;
    }
    
    @Override
    public PacketManager1_21_R3 packetManager() {
        return packetManager;
    }

    @Override
    public VersionHelper1_21_R3 versionHelper() {
        return versionHelper;
    }

    @Override
    protected PlayerAdapter createAdapter(final Player player) {
        return new PlayerAdapter1_21_R3(this, player);
    }

    @Override
    protected void terminateAdapter(final PlayerAdapter adapter) {
        if (!(adapter instanceof PlayerAdapter1_21_R3)) {
            return;
        }
        ((PlayerAdapter1_21_R3) adapter).terminateAdapter();
    }

    @Override
    protected LevelAdapter createAdapter(final World world) {
        if (!(world instanceof CraftWorld)) {
            return null;
        }
        return new LevelAdapter1_21_R3(this, ((CraftWorld) world).getHandle());
    }

    @Override
    protected void terminateAdapter(final LevelAdapter adapter) {}

}