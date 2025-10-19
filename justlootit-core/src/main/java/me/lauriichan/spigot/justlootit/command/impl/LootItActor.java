package me.lauriichan.spigot.justlootit.command.impl;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.lauriichan.laylib.command.Action;
import me.lauriichan.laylib.command.ActionMessage;
import me.lauriichan.laylib.command.Action.ActionType;
import me.lauriichan.minecraft.pluginbase.command.BukkitActor;
import me.lauriichan.minecraft.pluginbase.util.attribute.Attributes;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.VersionHelper;

public class LootItActor<P extends CommandSender> extends BukkitActor<P> {

    private final boolean isConsole, isRemote, isPlayer;

    private final Attributes attributes = new Attributes();

    private volatile NamespacedKey bossBarKey;
    private volatile BossBar bossBar;

    public LootItActor(final P handle, final JustLootItPlugin plugin) {
        super(handle, plugin);
        isConsole = handle instanceof ConsoleCommandSender;
        isRemote = handle instanceof RemoteConsoleCommandSender;
        isPlayer = handle instanceof Player;
    }

    public boolean isConsole() {
        return isConsole;
    }

    public boolean isRemoteConsole() {
        return isRemote;
    }

    public boolean isPlayer() {
        return isPlayer;
    }

    @Override
    public JustLootItPlugin plugin() {
        return (JustLootItPlugin) super.plugin();
    }

    public VersionHandler versionHandler() {
        return plugin().versionHandler();
    }

    public VersionHelper versionHelper() {
        return plugin().versionHelper();
    }

    public Attributes attributes() {
        return attributes;
    }

    @Override
    public void sendActionMessage(final ActionMessage message) {
        if (message != null && message.hoverAction() != null) {
            final Action hoverAction = message.hoverAction();
            if (hoverAction.getType() == ActionType.HOVER_SHOW) {
                if (hoverAction.getValue() instanceof final ItemStack item) {
                    message.action(Action.hoverShow(versionHelper().createItemHover(item)));
                } else if (hoverAction.getValue() instanceof final Entity entity) {
                    message.action(Action.hoverShow(versionHelper().createEntityHover(entity)));
                }
            }
        }
        super.sendActionMessage(message);
    }

    public boolean hasBossBar() {
        return bossBar != null;
    }

    public BossBar bossBar() {
        if (!isPlayer) {
            throw new UnsupportedOperationException("Only available for player");
        }
        if (bossBar != null) {
            return bossBar;
        }
        if (bossBarKey == null) {
            bossBarKey = new NamespacedKey(plugin(), getId().toString());
        }
        Server server = Bukkit.getServer();
        bossBar = server.getBossBar(bossBarKey);
        if (bossBar == null) {
            bossBar = server.createBossBar(bossBarKey, "BossBar", BarColor.PURPLE, BarStyle.SOLID);
            bossBar.setVisible(false);
        }
        bossBar.removeAll();
        bossBar.addPlayer((Player) handle);
        return bossBar;
    }

    public ActorNotifierBuilder<P> newNotifier() {
        return new ActorNotifierBuilder<>(this);
    }

    public void disconnect() {
        if (!isPlayer) {
            return;
        }
        if (bossBarKey != null) {
            Bukkit.removeBossBar(bossBarKey);
        }
    }

}
