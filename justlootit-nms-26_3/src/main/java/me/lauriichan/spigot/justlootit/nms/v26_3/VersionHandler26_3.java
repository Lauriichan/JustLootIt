package me.lauriichan.spigot.justlootit.nms.v26_3;

import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import me.lauriichan.spigot.justlootit.nms.IServiceProvider;
import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.packet.PacketOutSetEntityData;
import me.lauriichan.spigot.justlootit.nms.v26_3.convert.ConversionAdapter26_3;
import me.lauriichan.spigot.justlootit.nms.v26_3.debug.Debug26_3;
import me.lauriichan.spigot.justlootit.nms.v26_3.io.ItemStackIO26_3;
import me.lauriichan.spigot.justlootit.nms.v26_3.nbt.NbtHelper26_3;
import me.lauriichan.spigot.justlootit.nms.v26_3.network.PacketManager26_3;
import me.lauriichan.spigot.justlootit.nms.v26_3.packet.*;
import me.lauriichan.spigot.justlootit.nms.v26_3.util.random.RandomHelperImpl;
import me.lauriichan.spigot.justlootit.nms.version.VersionImpl;
import me.lauriichan.spigot.justlootit.platform.PlatformType;
import net.minecraft.network.protocol.game.*;

@VersionImpl(name = "spigot_26.3", versions = {
    "26.3"
}, platforms = {
    PlatformType.SPIGOT
})
public final class VersionHandler26_3 extends VersionHandler {

    private final NbtHelper26_3 nbtHelper = new NbtHelper26_3();

    private final PacketManager26_3 packetManager;
    private final VersionHelper26_3 versionHelper;

    private final ConversionAdapter26_3 conversionAdapter;

    public VersionHandler26_3(final IServiceProvider provider) {
        super(provider, new Debug26_3());
        this.packetManager = new PacketManager26_3(this);
        this.versionHelper = new VersionHelper26_3(this);
        this.conversionAdapter = new ConversionAdapter26_3(this);
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
        packetManager.register(ClientboundAddEntityPacket.class, PacketOutAddEntity26_3::new);
        packetManager.register(ClientboundSetEntityDataPacket.class, PacketOutSetEntityData26_3::new);
        // Outgoing packets (adapter)
        packetManager.registerAdapter(PacketOutSetEntityData.class, PacketOutSetEntityData26_3::new);
    }

    private void registerIO() {
        io.register(ItemStackIO26_3.ITEM_STACK);
    }

    @Override
    public NbtHelper26_3 nbtHelper() {
        return nbtHelper;
    }
    
    @Override
    public RandomHelperImpl randomHelper() {
        return RandomHelperImpl.INSTANCE;
    }

    @Override
    public ConversionAdapter26_3 conversionAdapter() {
        return conversionAdapter;
    }

    @Override
    public PacketManager26_3 packetManager() {
        return packetManager;
    }

    @Override
    public VersionHelper26_3 versionHelper() {
        return versionHelper;
    }

    @Override
    protected PlayerAdapter createAdapter(final Player player) {
        return new PlayerAdapter26_3(this, player);
    }

    @Override
    protected void terminateAdapter(final PlayerAdapter adapter) {
        if (!(adapter instanceof PlayerAdapter26_3)) {
            return;
        }
        ((PlayerAdapter26_3) adapter).terminateAdapter();
    }

    @Override
    protected LevelAdapter createAdapter(final World world) {
        if (!(world instanceof CraftWorld)) {
            return null;
        }
        return new LevelAdapter26_3(this, ((CraftWorld) world).getHandle());
    }

    @Override
    protected void terminateAdapter(final LevelAdapter adapter) {}

}