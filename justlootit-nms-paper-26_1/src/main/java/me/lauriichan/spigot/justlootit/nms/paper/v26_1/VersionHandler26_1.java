package me.lauriichan.spigot.justlootit.nms.paper.v26_1;

import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import me.lauriichan.spigot.justlootit.nms.IServiceProvider;
import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.packet.PacketOutSetEntityData;
import me.lauriichan.spigot.justlootit.nms.paper.v26_1.convert.ConversionAdapter26_1;
import me.lauriichan.spigot.justlootit.nms.paper.v26_1.debug.Debug26_1;
import me.lauriichan.spigot.justlootit.nms.paper.v26_1.io.ItemStackIO26_1;
import me.lauriichan.spigot.justlootit.nms.paper.v26_1.nbt.NbtHelper26_1;
import me.lauriichan.spigot.justlootit.nms.paper.v26_1.network.PacketManager26_1;
import me.lauriichan.spigot.justlootit.nms.paper.v26_1.packet.*;
import net.minecraft.network.protocol.game.*;

public final class VersionHandler26_1 extends VersionHandler {
    
    private final NbtHelper26_1 nbtHelper = new NbtHelper26_1();

    private final PacketManager26_1 packetManager;
    private final VersionHelper26_1 versionHelper;
    
    private final ConversionAdapter26_1 conversionAdapter;

    public VersionHandler26_1(final IServiceProvider provider) {
        super(provider, new Debug26_1());
        this.packetManager = new PacketManager26_1(this);
        this.versionHelper = new VersionHelper26_1(this);
        this.conversionAdapter = new ConversionAdapter26_1(this);
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
        packetManager.register(ClientboundAddEntityPacket.class, PacketOutAddEntity26_1::new);
        packetManager.register(ClientboundSetEntityDataPacket.class, PacketOutSetEntityData26_1::new);
        // Outgoing packets (adapter)
        packetManager.registerAdapter(PacketOutSetEntityData.class, PacketOutSetEntityData26_1::new);
    }

    private void registerIO() {
        io.register(ItemStackIO26_1.ITEM_STACK);
    }
    
    @Override
    public NbtHelper26_1 nbtHelper() {
        return nbtHelper;
    }

    @Override
    public ConversionAdapter26_1 conversionAdapter() {
        return conversionAdapter;
    }
    
    @Override
    public PacketManager26_1 packetManager() {
        return packetManager;
    }

    @Override
    public VersionHelper26_1 versionHelper() {
        return versionHelper;
    }

    @Override
    protected PlayerAdapter createAdapter(final Player player) {
        return new PlayerAdapter26_1(this, player);
    }

    @Override
    protected void terminateAdapter(final PlayerAdapter adapter) {
        if (!(adapter instanceof PlayerAdapter26_1)) {
            return;
        }
        ((PlayerAdapter26_1) adapter).terminateAdapter();
    }

    @Override
    protected LevelAdapter createAdapter(final World world) {
        if (!(world instanceof CraftWorld)) {
            return null;
        }
        return new LevelAdapter26_1(this, ((CraftWorld) world).getHandle());
    }

    @Override
    protected void terminateAdapter(final LevelAdapter adapter) {}

}