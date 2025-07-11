package me.lauriichan.spigot.justlootit.nms.v1_21_R5;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R5.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import me.lauriichan.spigot.justlootit.nms.IServiceProvider;
import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.packet.PacketOutSetEntityData;
import me.lauriichan.spigot.justlootit.nms.v1_21_R5.convert.ConversionAdapter1_21_R5;
import me.lauriichan.spigot.justlootit.nms.v1_21_R5.debug.Debug1_21_R5;
import me.lauriichan.spigot.justlootit.nms.v1_21_R5.io.ItemStackIO1_21_R5;
import me.lauriichan.spigot.justlootit.nms.v1_21_R5.nbt.NbtHelper1_21_R5;
import me.lauriichan.spigot.justlootit.nms.v1_21_R5.network.PacketManager1_21_R5;
import me.lauriichan.spigot.justlootit.nms.v1_21_R5.packet.*;
import net.minecraft.network.protocol.game.*;

public final class VersionHandler1_21_R5 extends VersionHandler {
    
    private final NbtHelper1_21_R5 nbtHelper = new NbtHelper1_21_R5();

    private final PacketManager1_21_R5 packetManager;
    private final VersionHelper1_21_R5 versionHelper;
    
    private final ConversionAdapter1_21_R5 conversionAdapter;

    public VersionHandler1_21_R5(final IServiceProvider provider) {
        super(provider, new Debug1_21_R5());
        this.packetManager = new PacketManager1_21_R5(this);
        this.versionHelper = new VersionHelper1_21_R5(this);
        this.conversionAdapter = new ConversionAdapter1_21_R5(this);
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
        packetManager.register(ClientboundAddEntityPacket.class, PacketOutAddEntity1_21_R5::new);
        packetManager.register(ClientboundSetEntityDataPacket.class, PacketOutSetEntityData1_21_R5::new);
        // Outgoing packets (adapter)
        packetManager.registerAdapter(PacketOutSetEntityData.class, PacketOutSetEntityData1_21_R5::new);
    }

    private void registerIO() {
        io.register(ItemStackIO1_21_R5.ITEM_STACK);
    }
    
    @Override
    public NbtHelper1_21_R5 nbtHelper() {
        return nbtHelper;
    }

    @Override
    public ConversionAdapter1_21_R5 conversionAdapter() {
        return conversionAdapter;
    }
    
    @Override
    public PacketManager1_21_R5 packetManager() {
        return packetManager;
    }

    @Override
    public VersionHelper1_21_R5 versionHelper() {
        return versionHelper;
    }

    @Override
    protected PlayerAdapter createAdapter(final Player player) {
        return new PlayerAdapter1_21_R5(this, player);
    }

    @Override
    protected void terminateAdapter(final PlayerAdapter adapter) {
        if (!(adapter instanceof PlayerAdapter1_21_R5)) {
            return;
        }
        ((PlayerAdapter1_21_R5) adapter).terminateAdapter();
    }

    @Override
    protected LevelAdapter createAdapter(final World world) {
        if (!(world instanceof CraftWorld)) {
            return null;
        }
        return new LevelAdapter1_21_R5(this, ((CraftWorld) world).getHandle());
    }

    @Override
    protected void terminateAdapter(final LevelAdapter adapter) {}

}