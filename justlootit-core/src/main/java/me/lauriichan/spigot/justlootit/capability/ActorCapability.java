package me.lauriichan.spigot.justlootit.capability;

import org.bukkit.entity.Player;

import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.command.impl.LootItActor;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.capability.ICapability;

public final class ActorCapability implements ICapability {

    public static LootItActor<Player> actor(JustLootItPlugin plugin, Player player) {
        return actor(plugin.versionHandler(), player);
    }

    public static LootItActor<Player> actor(VersionHandler handler, Player player) {
        return handler.getPlayer(player).getCapability(ActorCapability.class).get().actor();
    }

    public static LootItActor<Player> actor(PlayerAdapter adapter) {
        return adapter.getCapability(ActorCapability.class).get().actor();
    }

    private final PlayerAdapter adapter;
    private final JustLootItPlugin plugin;

    private LootItActor<Player> actor;

    public ActorCapability(PlayerAdapter adapter) {
        this.adapter = adapter;
        this.plugin = (JustLootItPlugin) adapter.versionHandler().plugin();
    }

    public LootItActor<Player> actor() {
        if (actor == null) {
            Player player = adapter.asBukkit();
            if (player == null) {
                return null;
            }
            return this.actor = new LootItActor<>(player, plugin);
        }
        return actor;
    }

    @Override
    public void terminate() {
        if (actor == null) {
            return;
        }
        actor.disconnect();
    }

}
