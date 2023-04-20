package me.lauriichan.spigot.justlootit.command.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.CommandManager;
import me.lauriichan.laylib.command.Node;
import me.lauriichan.laylib.command.NodeCommand;
import me.lauriichan.laylib.command.util.Triple;
import me.lauriichan.laylib.localization.MessageManager;
import me.lauriichan.laylib.reflection.JavaAccess;
import me.lauriichan.spigot.justlootit.nms.VersionHelper;

public final class BukkitCommandInjectedBridge implements CommandExecutor, TabCompleter {

    public static record CommandDefinition(String prefix, String name, List<String> aliases, String description) {

        public CommandDefinition(final String prefix, final String name, final List<String> aliases, final String description) {
            this.prefix = prefix;
            this.name = name;
            this.aliases = Collections.unmodifiableList(aliases);
            this.description = description;
        }

        public static final Builder of(final String name) {
            return new Builder(name);
        }

        public static final class Builder {

            private final String name;
            private final ArrayList<String> aliases = new ArrayList<>();
            private String description;
            private String prefix;

            private Builder(final String name) {
                this.name = name;
            }

            public Builder prefix(final String prefix) {
                this.prefix = prefix;
                return this;
            }

            public Builder alias(final String alias) {
                if (aliases.contains(alias)) {
                    return this;
                }
                aliases.add(alias);
                return this;
            }

            public Builder description(final String description) {
                this.description = description;
                return this;
            }

            public CommandDefinition build(final Plugin plugin) {
                String prefix = this.prefix;
                if (prefix == null || prefix.isBlank()) {
                    prefix = plugin.getName();
                }
                return new CommandDefinition(prefix.toLowerCase(Locale.ROOT), name, aliases, description);
            }

        }

    }

    private static final String[] EMPTY_ARGS = {};

    private final Plugin plugin;
    private final VersionHelper versionHelper;
    private final CommandManager commandManager;
    private final MessageManager messageManager;

    private final CommandDefinition definition;

    private final BukkitCommandListener listener;

    private volatile String fallbackCommand = "help";
    private volatile boolean injected = false;

    public BukkitCommandInjectedBridge(final Plugin plugin, final VersionHelper versionHelper, final CommandManager commandManager,
        final MessageManager messageManager, final CommandDefinition definition) {
        this.plugin = plugin;
        this.versionHelper = versionHelper;
        this.commandManager = commandManager;
        this.messageManager = messageManager;
        this.definition = definition;
        this.listener = new BukkitCommandListener(versionHelper, commandManager, messageManager);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final String commandName = args.length == 0 ? fallbackCommand : args[0];
        final String[] newArgs = args.length <= 1 ? EMPTY_ARGS : new String[args.length - 1];
        if (newArgs.length != 0) {
            System.arraycopy(args, 1, newArgs, 0, newArgs.length);
        }
        commandManager.createProcess(new BukkitActor<>(sender, messageManager, versionHelper), commandName, newArgs);
        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        final String commandName = args.length == 0 ? "" : args[0];
        final String[] newArgs = args.length <= 1 ? EMPTY_ARGS : new String[args.length - 1];
        if (newArgs.length != 0) {
            System.arraycopy(args, 1, newArgs, 0, newArgs.length);
        }
        final Triple<NodeCommand, Node, String> triple = commandManager.findNode(commandName, newArgs);
        if (triple == null) {
            if (newArgs.length == 1) {
                return Arrays.asList(commandManager.getCommands());
            }
            return null;
        }
        if (!triple.getB().hasChildren()) {
            return null;
        }
        return Arrays.asList(triple.getB().getNames());
    }

    /*
     * Management
     */

    public BukkitCommandInjectedBridge fallbackCommand(final String fallbackCommand) {
        this.fallbackCommand = fallbackCommand;
        return this;
    }

    public String fallbackCommand() {
        return fallbackCommand;
    }

    public CommandDefinition definition() {
        return definition;
    }

    public boolean injected() {
        return injected;
    }

    public BukkitCommandInjectedBridge inject() {
        if (injected) {
            return this;
        }
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        final SimpleCommandMap commandMap = (SimpleCommandMap) JavaAccess.invoke(Bukkit.getServer(),
            BukkitCommandInjector.craftServerGetCommandMap);
        final PluginCommand pluginCommand = (PluginCommand) JavaAccess.instance(BukkitCommandInjector.pluginCommandConstructor,
            definition.name(), plugin);
        pluginCommand.setAliases(new ArrayList<>(definition.aliases()));
        pluginCommand.setExecutor(this);
        pluginCommand.setTabCompleter(this);
        pluginCommand.setDescription(messageManager.translate(definition.description(), Actor.DEFAULT_LANGUAGE));
        commandMap.register(definition.prefix(), pluginCommand);
        return this;
    }

    @SuppressWarnings("unchecked")
    public BukkitCommandInjectedBridge uninject() {
        if (!injected) {
            return this;
        }
        HandlerList.unregisterAll(listener);
        final SimpleCommandMap commandMap = (SimpleCommandMap) JavaAccess.invoke(Bukkit.getServer(),
            BukkitCommandInjector.craftServerGetCommandMap);
        final Map<String, org.bukkit.command.Command> map = (Map<String, org.bukkit.command.Command>) JavaAccess.invoke(commandMap,
            BukkitCommandInjector.commandMapGetCommands);
        final ArrayList<String> names = new ArrayList<>();
        names.addAll(definition.aliases());
        names.add(definition.name());
        for (final String name : names) {
            org.bukkit.command.Command command = map.remove(name);
            if (command instanceof PluginCommand && ((PluginCommand) command).getPlugin().equals(plugin)) {
                command.unregister(commandMap);
            }
            command = map.remove(definition.prefix() + ':' + name);
            if (command != null) {
                command.unregister(commandMap);
            }
        }
        return this;
    }

}
