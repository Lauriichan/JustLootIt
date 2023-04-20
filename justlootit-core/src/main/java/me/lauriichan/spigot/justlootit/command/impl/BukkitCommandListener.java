package me.lauriichan.spigot.justlootit.command.impl;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.CommandManager;
import me.lauriichan.laylib.command.CommandProcess;
import me.lauriichan.laylib.localization.MessageManager;
import me.lauriichan.spigot.justlootit.nms.VersionHelper;

public final class BukkitCommandListener implements Listener {

    private final VersionHelper versionHelper;
    private final CommandManager commandManager;
    private final MessageManager messageManager;

    public BukkitCommandListener(final VersionHelper versionHelper, final CommandManager commandManager,
        final MessageManager messageManager) {
        this.versionHelper = versionHelper;
        this.commandManager = commandManager;
        this.messageManager = messageManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final CommandProcess process = commandManager.getProcess(player.getUniqueId());
        if (process == null) {
            return;
        }
        event.setCancelled(true);
        commandManager.handleProcessInput(new BukkitActor<>(player, messageManager, versionHelper), process, event.getMessage(), false);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPreProcess(final PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        final CommandProcess process = commandManager.getProcess(player.getUniqueId());
        if (process == null) {
            return;
        }
        event.setCancelled(true);
        final BukkitActor<Player> actor = new BukkitActor<>(player, messageManager, versionHelper);
        final String[] args = event.getMessage().split(" ");
        if ("/cancel".equalsIgnoreCase(args[0])) {
            commandManager.cancelProcess(actor);
            return;
        }
        if ("/skip".equalsIgnoreCase(args[0])) {
            commandManager.handleProcessSkip(actor, process);
            return;
        }
        if (args.length > 1 && "/suggestion".equalsIgnoreCase(args[0])) {
            commandManager.handleProcessInput(actor, process,
                Arrays.stream(args).skip(1).filter(Predicate.not(String::isBlank)).collect(Collectors.joining(" ")), true);
            return;
        }
        commandManager.handleProcessInput(actor, process, event.getMessage());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onServerCommand(final ServerCommandEvent event) {
        final CommandProcess process = commandManager.getProcess(Actor.IMPL_ID);
        if (process == null) {
            return;
        }
        final BukkitActor<CommandSender> actor = new BukkitActor<>(event.getSender(), messageManager, versionHelper);
        event.setCancelled(true);
        final String[] args = event.getCommand().split(" ");
        if ("/cancel".equalsIgnoreCase(args[0])) {
            commandManager.cancelProcess(actor);
            return;
        }
        if ("/skip".equalsIgnoreCase(args[0])) {
            commandManager.handleProcessSkip(actor, process);
            return;
        }
        if (args.length > 1 && "/suggestion".equalsIgnoreCase(args[0])) {
            commandManager.handleProcessInput(actor, process,
                Arrays.stream(args).skip(1).filter(Predicate.not(String::isBlank)).collect(Collectors.joining(" ")), true);
            return;
        }
        commandManager.handleProcessInput(actor, process, event.getCommand());
    }

}
