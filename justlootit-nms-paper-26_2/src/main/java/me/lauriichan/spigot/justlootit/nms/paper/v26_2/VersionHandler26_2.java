package me.lauriichan.spigot.justlootit.nms.paper.v26_2;

import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import me.lauriichan.spigot.justlootit.nms.IServiceProvider;
import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.packet.PacketOutSetEntityData;
import me.lauriichan.spigot.justlootit.nms.paper.v26_2.convert.ConversionAdapter26_2;
import me.lauriichan.spigot.justlootit.nms.paper.v26_2.debug.Debug26_2;
import me.lauriichan.spigot.justlootit.nms.paper.v26_2.io.ItemStackIO26_2;
import me.lauriichan.spigot.justlootit.nms.paper.v26_2.nbt.NbtHelper26_2;
import me.lauriichan.spigot.justlootit.nms.paper.v26_2.network.PacketManager26_2;
import me.lauriichan.spigot.justlootit.nms.paper.v26_2.packet.*;
import me.lauriichan.spigot.justlootit.nms.version.VersionImpl;
import me.lauriichan.spigot.justlootit.platform.PlatformType;
import net.minecraft.network.protocol.game.*;

@VersionImpl(name = "paper_26.2", versions = {
    "26.2"
}, platforms = {
    PlatformType.PAPER,
    PlatformType.FOLIA
})
public final class VersionHandler26_2 extends VersionHandler {

    private final NbtHelper26_2 nbtHelper = new NbtHelper26_2();

    private final PacketManager26_2 packetManager;
    private final VersionHelper26_2 versionHelper;

    private final ConversionAdapter26_2 conversionAdapter;

    public VersionHandler26_2(final IServiceProvider provider) {
        super(provider, new Debug26_2());
        this.packetManager = new PacketManager26_2(this);
        this.versionHelper = new VersionHelper26_2(this);
        this.conversionAdapter = new ConversionAdapter26_2(this);
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
        packetManager.register(ClientboundAddEntityPacket.class, PacketOutAddEntity26_2::new);
        packetManager.register(ClientboundSetEntityDataPacket.class, PacketOutSetEntityData26_2::new);
        // Outgoing packets (adapter)
        packetManager.registerAdapter(PacketOutSetEntityData.class, PacketOutSetEntityData26_2::new);
    }

    private void registerIO() {
        io.register(ItemStackIO26_2.ITEM_STACK);
    }

    @Override
    public NbtHelper26_2 nbtHelper() {
        return nbtHelper;
    }

    @Override
    public ConversionAdapter26_2 conversionAdapter() {
        return conversionAdapter;
    }

    @Override
    public PacketManager26_2 packetManager() {
        return packetManager;
    }

    @Override
    public VersionHelper26_2 versionHelper() {
        return versionHelper;
    }

    @Override
    protected PlayerAdapter createAdapter(final Player player) {
        return new PlayerAdapter26_2(this, player);
    }

    @Override
    protected void terminateAdapter(final PlayerAdapter adapter) {
        if (!(adapter instanceof PlayerAdapter26_2)) {
            return;
        }
        ((PlayerAdapter26_2) adapter).terminateAdapter();
    }

    @Override
    protected LevelAdapter createAdapter(final World world) {
        if (!(world instanceof CraftWorld)) {
            return null;
        }
        return new LevelAdapter26_2(this, ((CraftWorld) world).getHandle());
    }

    @Override
    protected void terminateAdapter(final LevelAdapter adapter) {}

}