package me.lauriichan.spigot.justlootit.nms;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import me.lauriichan.spigot.justlootit.nms.packet.listener.PacketManager;

public abstract class VersionHandler {

    protected final PlayerListener playerListener = new PlayerListener(this);

    protected final ConcurrentHashMap<UUID, PlayerAdapter> players = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<UUID, LevelAdapter> levels = new ConcurrentHashMap<>();

    protected final IServiceProvider provider;

    public VersionHandler(final IServiceProvider provider) {
        this.provider = provider;
    }

    /*
     * Startup and shutdown
     */

    public final void enable() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        onEnable(pluginManager);
        pluginManager.registerEvents(playerListener, provider.plugin());
        for (Player player : Bukkit.getOnlinePlayers()) {
            join(player);
        }
    }

    public final void disable() {
        HandlerList.unregisterAll(playerListener);
        for (Player player : Bukkit.getOnlinePlayers()) {
            quit(player);
        }
        onDisable();
    }

    protected void onEnable(final PluginManager pluginManager) {}

    protected void onDisable() {}

    /*
     * Player management
     */

    public final PlayerAdapter getPlayer(UUID playerId) {
        if (players.containsKey(playerId)) {
            return players.get(playerId);
        }
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return null;
        }
        PlayerAdapter adapter = createAdapter(player);
        players.put(playerId, adapter);
        return adapter;
    }

    public final PlayerAdapter getPlayer(Player player) {
        if (player == null) {
            return null;
        }
        UUID playerId = player.getUniqueId();
        if (players.containsKey(playerId)) {
            return players.get(playerId);
        }
        PlayerAdapter adapter = createAdapter(player);
        players.put(playerId, adapter);
        return adapter;
    }

    final void join(Player player) {
        if (players.containsKey(player.getUniqueId())) {
            return;
        }
        players.put(player.getUniqueId(), createAdapter(player));
    }

    final void quit(Player player) {
        if (!players.containsKey(player.getUniqueId())) {
            return;
        }
        terminateAdapter(players.remove(player.getUniqueId()));
    }

    protected abstract PlayerAdapter createAdapter(Player player);

    protected abstract void terminateAdapter(PlayerAdapter adapter);

    /*
     * Level management
     */

    public final LevelAdapter getLevel(UUID levelId) {
        if (levels.containsKey(levelId)) {
            return levels.get(levelId);
        }
        World world = Bukkit.getWorld(levelId);
        if (world == null) {
            return null;
        }
        LevelAdapter adapter = createAdapter(world);
        levels.put(levelId, adapter);
        return adapter;
    }

    public final LevelAdapter getLevel(World world) {
        if (world == null) {
            return null;
        }
        UUID levelId = world.getUID();
        if (levels.containsKey(levelId)) {
            return levels.get(levelId);
        }
        LevelAdapter adapter = createAdapter(world);
        levels.put(levelId, adapter);
        return adapter;
    }

    final void load(World world) {
        if (levels.containsKey(world.getUID())) {
            return;
        }
        levels.put(world.getUID(), createAdapter(world));
    }

    final void unload(World world) {
        if (!levels.containsKey(world.getUID())) {
            return;
        }
        terminateAdapter(levels.remove(world.getUID()));
    }

    protected abstract LevelAdapter createAdapter(World world);

    protected abstract void terminateAdapter(LevelAdapter adapter);

    /*
     * Getter
     */

    public abstract PacketManager getPacketManager();

    public abstract VersionHelper getVersionHelper();

    public final Plugin plugin() {
        return provider.plugin();
    }

    public final ExecutorService mainService() {
        return provider.mainService();
    }

    public final ExecutorService asyncService() {
        return provider.asyncService();
    }

}
