package me.lauriichan.spigot.justlootit.command.impl;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import me.lauriichan.laylib.command.CommandManager;
import me.lauriichan.laylib.command.Node;
import me.lauriichan.laylib.command.NodeCommand;
import me.lauriichan.laylib.command.util.Triple;
import me.lauriichan.laylib.localization.MessageManager;
import me.lauriichan.spigot.justlootit.nms.VersionHelper;

public final class BukkitCommandBridge implements CommandExecutor, TabCompleter {

    private final VersionHelper versionHelper;
    private final CommandManager commandManager;
    private final MessageManager messageManager;
    private final String prefix;

    public BukkitCommandBridge(final VersionHelper versionHelper, final CommandManager commandManager, final MessageManager messageManager, final String prefix) {
        this.versionHelper = versionHelper;
        this.commandManager = commandManager;
        this.messageManager = messageManager;
        this.prefix = prefix + ':';
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        while (label.startsWith("/")) {
            label = label.substring(1);
        }
        if (label.startsWith(prefix)) {
            label = label.substring(prefix.length());
        }
        commandManager.createProcess(new BukkitActor<>(sender, messageManager, versionHelper), label, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        while (label.startsWith("/")) {
            label = label.substring(1);
        }
        if (label.startsWith(prefix)) {
            label = label.substring(prefix.length());
        }
        Triple<NodeCommand, Node, String> triple = commandManager.findNode(label, args);
        if (triple == null) {
            return null;
        }
        if (!triple.getB().hasChildren()) {
            return null;
        }
        return Arrays.asList(triple.getB().getNames());
    }

}
