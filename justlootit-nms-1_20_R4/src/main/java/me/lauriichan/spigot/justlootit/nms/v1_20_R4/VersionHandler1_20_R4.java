package me.lauriichan.spigot.justlootit.nms.v1_20_R4;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R4.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import me.lauriichan.spigot.justlootit.nms.IServiceProvider;
import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.packet.PacketOutSetEntityData;
import me.lauriichan.spigot.justlootit.nms.v1_20_R4.convert.ConversionAdapter1_20_R4;
import me.lauriichan.spigot.justlootit.nms.v1_20_R4.debug.Debug1_20_R4;
import me.lauriichan.spigot.justlootit.nms.v1_20_R4.io.ItemStackIO1_20_R4;
import me.lauriichan.spigot.justlootit.nms.v1_20_R4.nbt.NbtHelper1_20_R4;
import me.lauriichan.spigot.justlootit.nms.v1_20_R4.network.PacketManager1_20_R4;
import me.lauriichan.spigot.justlootit.nms.v1_20_R4.packet.*;
import net.minecraft.network.protocol.game.*;

public final class VersionHandler1_20_R4 extends VersionHandler {
    
    private final NbtHelper1_20_R4 nbtHelper = new NbtHelper1_20_R4();

    private final PacketManager1_20_R4 packetManager;
    private final VersionHelper1_20_R4 versionHelper;
    
    private final ConversionAdapter1_20_R4 conversionAdapter;

    public VersionHandler1_20_R4(final IServiceProvider provider) {
        super(provider, new Debug1_20_R4());
        this.packetManager = new PacketManager1_20_R4(this);
        this.versionHelper = new VersionHelper1_20_R4(this);
        this.conversionAdapter = new ConversionAdapter1_20_R4(this);
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
        packetManager.register(ClientboundAddEntityPacket.class, PacketOutAddEntity1_20_R4::new);
        packetManager.register(ClientboundSetEntityDataPacket.class, PacketOutSetEntityData1_20_R4::new);
        // Outgoing packets (adapter)
        packetManager.registerAdapter(PacketOutSetEntityData.class, PacketOutSetEntityData1_20_R4::new);
    }

    private void registerIO() {
        io.register(ItemStackIO1_20_R4.ITEM_STACK);
    }
    
    @Override
    public NbtHelper1_20_R4 nbtHelper() {
        return nbtHelper;
    }

    @Override
    public ConversionAdapter1_20_R4 conversionAdapter() {
        return conversionAdapter;
    }
    
    @Override
    public PacketManager1_20_R4 packetManager() {
        return packetManager;
    }

    @Override
    public VersionHelper1_20_R4 versionHelper() {
        return versionHelper;
    }

    @Override
    protected PlayerAdapter createAdapter(final Player player) {
        return new PlayerAdapter1_20_R4(this, player);
    }

    @Override
    protected void terminateAdapter(final PlayerAdapter adapter) {
        if (!(adapter instanceof PlayerAdapter1_20_R4)) {
            return;
        }
        ((PlayerAdapter1_20_R4) adapter).terminateAdapter();
    }

    @Override
    protected LevelAdapter createAdapter(final World world) {
        if (!(world instanceof CraftWorld)) {
            return null;
        }
        return new LevelAdapter1_20_R4(this, ((CraftWorld) world).getHandle());
    }

    @Override
    protected void terminateAdapter(final LevelAdapter adapter) {}

}