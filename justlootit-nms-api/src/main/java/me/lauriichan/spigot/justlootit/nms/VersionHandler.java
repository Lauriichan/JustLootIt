package me.lauriichan.spigot.justlootit.nms;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.nms.capability.CapabilityManager;
import me.lauriichan.spigot.justlootit.nms.capability.Capable;
import me.lauriichan.spigot.justlootit.nms.convert.ConversionAdapter;
import me.lauriichan.spigot.justlootit.nms.debug.DebugHelper;
import me.lauriichan.spigot.justlootit.nms.debug.IDebugHelper;
import me.lauriichan.spigot.justlootit.nms.io.IOProvider;
import me.lauriichan.spigot.justlootit.nms.nbt.NbtHelper;
import me.lauriichan.spigot.justlootit.nms.packet.AbstractPacketOut;
import me.lauriichan.spigot.justlootit.nms.packet.listener.PacketManager;
import me.lauriichan.spigot.justlootit.platform.JustLootItPlatform;
import me.lauriichan.spigot.justlootit.platform.scheduler.Scheduler;

public abstract class VersionHandler {

    protected final VersionListener bukkitListener = new VersionListener(this);

    protected final CapabilityManager capabilityManager = new CapabilityManager();
    protected final IOProvider io = new IOProvider();

    protected final ConcurrentHashMap<UUID, PlayerAdapter> players = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<UUID, LevelAdapter> levels = new ConcurrentHashMap<>();

    protected final IServiceProvider serviceProvider;
    
    protected final DebugHelper debugHelper;

    public VersionHandler(final IServiceProvider serviceProvider) {
        this(serviceProvider, null);
    }

    public VersionHandler(final IServiceProvider serviceProvider, final IDebugHelper debugHelper) {
        this.serviceProvider = serviceProvider;
        this.debugHelper = new DebugHelper(debugHelper);
    }

    /*
     * Startup and shutdown
     */

    public final void enable() {
        final PluginManager pluginManager = Bukkit.getPluginManager();
        onEnable(pluginManager);
        pluginManager.registerEvents(bukkitListener, serviceProvider.plugin());
        for (final World world : Bukkit.getWorlds()) {
            load(world);
        }
        for (final Player player : Bukkit.getOnlinePlayers()) {
            join(player);
        }
    }

    public final void disable() {
        HandlerList.unregisterAll(bukkitListener);
        for (final Player player : Bukkit.getOnlinePlayers()) {
            quit(player);
        }
        for (final World world : Bukkit.getWorlds()) {
            unload(world);
        }
        onDisable();
    }

    protected void onEnable(final PluginManager pluginManager) {}

    protected void onDisable() {}

    /*
     * Player management
     */

    public final void broadcast(final AbstractPacketOut... packets) {
        final PlayerAdapter[] adapters = players.values().toArray(PlayerAdapter[]::new);
        for (final PlayerAdapter adapter : adapters) {
            adapter.send(packets);
        }
    }

    public final PlayerAdapter getPlayer(final UUID playerId) {
        if (players.containsKey(playerId)) {
            return players.get(playerId);
        }
        final Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return null;
        }
        final PlayerAdapter adapter = createAdapter(player);
        players.put(playerId, applyCapabilities(adapter));
        return adapter;
    }

    public final PlayerAdapter getPlayer(final Player player) {
        if (player == null) {
            return null;
        }
        final UUID playerId = player.getUniqueId();
        if (players.containsKey(playerId)) {
            return players.get(playerId);
        }
        final PlayerAdapter adapter = createAdapter(player);
        players.put(playerId, applyCapabilities(adapter));
        return adapter;
    }

    final void join(final Player player) {
        if (players.containsKey(player.getUniqueId())) {
            return;
        }
        players.put(player.getUniqueId(), applyCapabilities(createAdapter(player)));
    }

    final void quit(final Player player) {
        final PlayerAdapter adapter = players.remove(player.getUniqueId());
        if (adapter == null) {
            return;
        }
        terminateAdapter(adapter);
        adapter.terminate();
    }

    protected abstract PlayerAdapter createAdapter(Player player);

    protected abstract void terminateAdapter(PlayerAdapter adapter);

    /*
     * Level management
     */

    public final LevelAdapter getLevel(final UUID levelId) {
        if (levels.containsKey(levelId)) {
            return levels.get(levelId);
        }
        final World world = Bukkit.getWorld(levelId);
        if (world == null) {
            return null;
        }
        final LevelAdapter adapter = createAdapter(world);
        levels.put(levelId, applyCapabilities(adapter));
        return adapter;
    }

    public final LevelAdapter getLevel(final World world) {
        if (world == null) {
            return null;
        }
        final UUID levelId = world.getUID();
        if (levels.containsKey(levelId)) {
            return levels.get(levelId);
        }
        final LevelAdapter adapter = createAdapter(world);
        levels.put(levelId, applyCapabilities(adapter));
        return adapter;
    }

    final void load(final World world) {
        if (levels.containsKey(world.getUID())) {
            return;
        }
        levels.put(world.getUID(), applyCapabilities(createAdapter(world)));
    }

    final void unload(final World world) {
        final LevelAdapter adapter = levels.remove(world.getUID());
        if (adapter == null) {
            return;
        }
        terminateAdapter(adapter);
        adapter.terminate();
    }

    protected abstract LevelAdapter createAdapter(World world);

    protected abstract void terminateAdapter(LevelAdapter adapter);

    /*
     * Capabilities
     */

    public final <T extends Capable<?>> T applyCapabilities(final T capable) {
        capabilityManager.forEach(provider -> capable.addCapabilities(this, provider));
        return capable;
    }

    /*
     * Getter
     */
    
    public abstract ConversionAdapter conversionAdapter();

    public abstract PacketManager packetManager();

    public abstract VersionHelper versionHelper();
    
    public abstract NbtHelper nbtHelper();
    
    public final IServiceProvider serviceProvider() {
        return serviceProvider;
    }
    
    public final IDebugHelper debugHelper() {
        return debugHelper;
    }

    public final CapabilityManager capabilities() {
        return capabilityManager;
    }

    public final IOProvider io() {
        return io;
    }

    public final Plugin plugin() {
        return serviceProvider.plugin();
    }
    
    public final File mainWorldFolder() {
        return serviceProvider.mainWorldFolder();
    }

    public final ISimpleLogger logger() {
        return serviceProvider.logger();
    }

    public final JustLootItPlatform platform() {
        return serviceProvider.platform();
    }

    public final Scheduler scheduler() {
        return serviceProvider.platform().scheduler();
    }

}
