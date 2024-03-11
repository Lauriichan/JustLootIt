package me.lauriichan.spigot.justlootit.command.impl;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import me.lauriichan.laylib.command.Action;
import me.lauriichan.laylib.command.ActionMessage;
import me.lauriichan.laylib.localization.MessageManager;
import me.lauriichan.minecraft.pluginbase.command.BukkitActor;
import me.lauriichan.minecraft.pluginbase.message.component.ComponentParser;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.VersionHelper;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;

public class LootItActor<P extends CommandSender> extends BukkitActor<P> {

    private final VersionHelper versionHelper;

    public LootItActor(final P handle, final MessageManager messageManager, final VersionHelper versionHelper) {
        super(handle, messageManager);
        this.versionHelper = versionHelper;
    }
    
    public VersionHandler versionHandler() {
        return versionHelper.handler();
    }
    
    public VersionHelper versionHelper() {
        return versionHelper;
    }
    
    @Override
    public void sendActionMessage(final ActionMessage message) {
        if (message == null) {
            return;
        }
        final String content = message.message();
        if (content == null || content.isBlank()) {
            handle.sendMessage("");
            return;
        }
        ClickEvent click = null;
        HoverEvent hover = null;
        if (message.clickAction() != null) {
            final Action clickAction = message.clickAction();
            switch (clickAction.getType()) {
            case CLICK_COPY:
                try {
                    click = new ClickEvent(ClickEvent.Action.valueOf("COPY_TO_CLIPBOARD"), clickAction.getValueAsString());
                } catch (final IllegalArgumentException exp) {
                }
                break;
            case CLICK_FILE:
                click = new ClickEvent(ClickEvent.Action.OPEN_FILE, clickAction.getValueAsString());
                break;
            case CLICK_RUN:
                click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, clickAction.getValueAsString());
                break;
            case CLICK_SUGGEST:
                click = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickAction.getValueAsString());
                break;
            case CLICK_URL:
                click = new ClickEvent(ClickEvent.Action.OPEN_URL, clickAction.getValueAsString());
                break;
            default:
                break;
            }
        }
        if (message.hoverAction() != null) {
            final Action hoverAction = message.hoverAction();
            switch (hoverAction.getType()) {
            case HOVER_SHOW:
                if (versionHelper == null) {
                    break;
                }
                if (hoverAction.getValue() instanceof final ItemStack item) {
                    hover = new HoverEvent(HoverEvent.Action.SHOW_ITEM,
                        new Item(item.getType().getKey().toString(), item.getAmount(), versionHelper.asItemTag(item)));
                } else if (hoverAction.getValue() instanceof final Entity entity) {
                    hover = new HoverEvent(HoverEvent.Action.SHOW_ENTITY, versionHelper.createEntityHover(entity));
                }
                break;
            case HOVER_TEXT:
                hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ComponentParser.parse(hoverAction.getValueAsString())));
                break;
            default:
                break;
            }
        }
        handle.spigot().sendMessage(ComponentParser.parse(message.message(), click, hover));
    }

}
