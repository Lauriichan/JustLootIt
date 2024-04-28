package me.lauriichan.spigot.justlootit.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.listener.IListenerExtension;
import me.lauriichan.spigot.justlootit.input.SimpleChatInputProvider;
import me.lauriichan.spigot.justlootit.input.SimpleChatInputProvider.ChatInput;
import me.lauriichan.spigot.justlootit.message.Messages;

@Extension
public class InputListener implements IListenerExtension {

    private final SimpleChatInputProvider provider = SimpleChatInputProvider.CHAT;
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onServerCommand(final ServerCommandEvent event) {
        ChatInput input = provider.get(Actor.IMPL_ID);
        if (input == null) {
            return;
        }
        event.setCancelled(true);
        String message = event.getCommand();
        if (message.equalsIgnoreCase(SimpleChatInputProvider.CANCEL_MESSAGE)) {
            input.remove();
            input.actor().sendTranslatedMessage(Messages.INPUT_MANUAL_CANCEL);
            try {
                input.consumer().accept(input.actor(), null);
            } catch(RuntimeException exp) {
                input.actor().sendTranslatedMessage(Messages.INPUT_SIMPLE_FAILED, Key.of("message", exp.getMessage()));
            }
            return;
        }
        if (!input.predicate().test(message)) {
            input.actor().sendMessage(input.retryMessage());
            return;
        }
        input.remove();
        try {
            input.consumer().accept(input.actor(), message);
        } catch(RuntimeException exp) {
            input.actor().sendTranslatedMessage(Messages.INPUT_SIMPLE_FAILED, Key.of("message", exp.getMessage()));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (provider.hasNoInputs()) {
            return;
        }
        ChatInput input = provider.get(event.getPlayer().getUniqueId());
        if (input == null) {
            return;
        }
        event.setCancelled(true);
        String message = event.getMessage();
        if (message.equalsIgnoreCase(SimpleChatInputProvider.CANCEL_MESSAGE)) {
            input.remove();
            input.actor().sendTranslatedMessage(Messages.INPUT_MANUAL_CANCEL);
            try {
                input.consumer().accept(input.actor(), null);
            } catch(RuntimeException exp) {
                input.actor().sendTranslatedMessage(Messages.INPUT_SIMPLE_FAILED, Key.of("message", exp.getMessage()));
            }
            return;
        }
        if (!input.predicate().test(message)) {
            input.actor().sendMessage(input.retryMessage());
            return;
        }
        input.remove();
        try {
            input.consumer().accept(input.actor(), message);
        } catch(RuntimeException exp) {
            input.actor().sendTranslatedMessage(Messages.INPUT_SIMPLE_FAILED, Key.of("message", exp.getMessage()));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        if (provider.hasNoInputs()) {
            return;
        }
        ChatInput input = provider.get(event.getPlayer().getUniqueId());
        if (input != null) {
            input.remove();
        }
    }

}
