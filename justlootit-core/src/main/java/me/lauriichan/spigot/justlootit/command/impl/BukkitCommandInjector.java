package me.lauriichan.spigot.justlootit.command.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.CommandManager;
import me.lauriichan.laylib.command.ICommandInjector;
import me.lauriichan.laylib.command.NodeCommand;
import me.lauriichan.laylib.localization.MessageManager;
import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.laylib.reflection.JavaAccess;
import me.lauriichan.spigot.justlootit.nms.VersionHelper;
import me.lauriichan.spigot.justlootit.util.VersionConstant;

public final class BukkitCommandInjector implements ICommandInjector {

    private final Constructor<?> pluginCommandConstructor = ClassUtil.getConstructor(PluginCommand.class, String.class, Plugin.class);
    private final Method craftServerGetCommandMap = ClassUtil.getMethod(ClassUtil.findClass(VersionConstant.craftClassPath("CraftServer")),
        "getCommandMap");
    private final Method commandMapGetCommands = ClassUtil.getMethod(SimpleCommandMap.class, "knownCommands");

    private final Plugin plugin;
    private final BukkitCommandBridge bridge;
    private final BukkitCommandListener listener;

    private final MessageManager messageManager;

    private final String prefix;

    public BukkitCommandInjector(final VersionHelper versionHelper, final CommandManager commandManager, final MessageManager messageManager, final Plugin plugin) {
        this.plugin = plugin;
        this.prefix = plugin.getName().toLowerCase(Locale.ROOT);
        this.bridge = new BukkitCommandBridge(versionHelper, commandManager, messageManager, prefix);
        this.listener = new BukkitCommandListener(versionHelper, commandManager, messageManager);
        this.messageManager = messageManager;
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    public BukkitCommandBridge getBridge() {
        return bridge;
    }

    @Override
    public void inject(NodeCommand nodeCommand) {
        final SimpleCommandMap commandMap = (SimpleCommandMap) JavaAccess.invoke(Bukkit.getServer(), craftServerGetCommandMap);
        final PluginCommand pluginCommand = (PluginCommand) JavaAccess.instance(pluginCommandConstructor, nodeCommand.getName(), plugin);
        pluginCommand.setAliases(new ArrayList<>(nodeCommand.getAliases()));
        pluginCommand.setExecutor(bridge);
        pluginCommand.setTabCompleter(bridge);
        pluginCommand.setDescription(messageManager.translate(nodeCommand.getDescription(), Actor.DEFAULT_LANGUAGE));
        commandMap.register(prefix, pluginCommand);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void uninject(NodeCommand nodeCommand) {
        final SimpleCommandMap commandMap = (SimpleCommandMap) JavaAccess.invoke(Bukkit.getServer(), craftServerGetCommandMap);
        final Map<String, org.bukkit.command.Command> map = (Map<String, org.bukkit.command.Command>) JavaAccess.invoke(commandMap,
            commandMapGetCommands);
        ArrayList<String> names = new ArrayList<>();
        names.addAll(nodeCommand.getAliases());
        names.add(nodeCommand.getName());
        for (final String name : names) {
            org.bukkit.command.Command command = map.remove(name);
            if (command instanceof PluginCommand && ((PluginCommand) command).getPlugin().equals(plugin)) {
                command.unregister(commandMap);
            }
            command = map.remove(prefix + ':' + name);
            if (command != null) {
                command.unregister(commandMap);
            }
        }
    }

}
