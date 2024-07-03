package me.lauriichan.spigot.justlootit.command.impl;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import me.lauriichan.laylib.command.Action;
import me.lauriichan.laylib.command.ActionMessage;
import me.lauriichan.laylib.command.Action.ActionType;
import me.lauriichan.minecraft.pluginbase.command.BukkitActor;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.VersionHelper;

public class LootItActor<P extends CommandSender> extends BukkitActor<P> {

    public LootItActor(final P handle, final JustLootItPlugin plugin) {
        super(handle, plugin);
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

}
