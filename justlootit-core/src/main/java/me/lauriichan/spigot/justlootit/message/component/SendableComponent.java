package me.lauriichan.spigot.justlootit.message.component;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.lauriichan.laylib.command.Actor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;

public abstract class SendableComponent {

    public static final BaseComponent[] EMPTY = {};

    public void send(final Actor<?> actor) {
        final Actor<CommandSender> sender = actor.as(CommandSender.class);
        if (!actor.isValid()) {
            return;
        }
        send(sender.getHandle());
    }

    public void send(final Actor<?> actor, final ChatMessageType type) {
        final Actor<Player> sender = actor.as(Player.class);
        if (!actor.isValid()) {
            return;
        }
        send(sender.getHandle(), type);
    }

    public void send(final CommandSender sender) {
        sender.spigot().sendMessage(build());
    }

    public void send(final Player player, final ChatMessageType type) {
        player.spigot().sendMessage(type, build());
    }

    public void broadcast(final World world) {
        broadcast(world, ChatMessageType.CHAT);
    }

    public void broadcast(final World world, final ChatMessageType type) {
        final BaseComponent[] message = build();
        for (final Player player : world.getPlayers()) {
            player.spigot().sendMessage(type, message);
        }
    }

    public void broadcast() {
        Bukkit.getServer().spigot().broadcast(build());
    }

    public abstract BaseComponent[] build();

}