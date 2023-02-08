package me.lauriichan.spigot.justlootit.command.provider;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.IProviderArgumentType;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;

public final class PluginProvider implements IProviderArgumentType<JustLootItPlugin> {

    private final JustLootItPlugin plugin;

    public PluginProvider(final JustLootItPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public JustLootItPlugin provide(Actor<?> actor) {
        return plugin;
    }

}
