package me.lauriichan.spigot.justlootit.message.component;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.lauriichan.laylib.command.Actor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;

public abstract class SendableComponent {

    public static final BaseComponent[] EMPTY = new BaseComponent[0];

    public void send(Actor<?> actor) {
        Actor<CommandSender> sender = actor.as(CommandSender.class);
        if (!actor.isValid()) {
            return;
        }
        send(sender.getHandle());
    }

    public void send(Actor<?> actor, ChatMessageType type) {
        Actor<Player> sender = actor.as(Player.class);
        if (!actor.isValid()) {
            return;
        }
        send(sender.getHandle(), type);
    }

    public void send(CommandSender sender) {
        sender.spigot().sendMessage(build());
    }

    public void send(Player player, ChatMessageType type) {
        player.spigot().sendMessage(type, build());
    }

    public void broadcast(World world) {
        broadcast(world, ChatMessageType.CHAT);
    }

    public void broadcast(World world, ChatMessageType type) {
        BaseComponent[] message = build();
        for (Player player : world.getPlayers()) {
            player.spigot().sendMessage(type, message);
        }
    }

    public void broadcast() {
        Bukkit.getServer().spigot().broadcast(build());
    }

    public abstract BaseComponent[] build();

}