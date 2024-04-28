package me.lauriichan.spigot.justlootit.compatibility.betterinputs;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.minecraft.pluginbase.util.color.BukkitColor;
import me.lauriichan.spigot.justlootit.input.InputProvider;
import me.lauriichan.spigot.justlootit.input.SimpleChatInputProvider;
import me.lauriichan.spigot.justlootit.message.Messages;
import me.nemo_64.betterinputs.api.BetterInputs;
import me.nemo_64.betterinputs.api.input.modifier.AttemptModifier;

public class BetterInputsInputProvider extends InputProvider {

    private static final String CHAT = "betterinputs:input/chat";
    private static final String ANVIL = "betterinputs:input/anvil";

    private static final int ATTEMPTS = 8;

    private final BetterInputs<?> api;

    public BetterInputsInputProvider() {
        api = Bukkit.getServicesManager().getRegistration(BetterInputs.class).getProvider();
    }

    @Override
    public void getStringInput(Actor<?> actor, String prompt, String retryMessage, Predicate<String> predicate,
        BiConsumer<Actor<?>, String> consumer) {
        if (!actor.as(Player.class).isValid()) {
            SimpleChatInputProvider.CHAT.getStringInput(actor, prompt, retryMessage, predicate, consumer);
            return;
        }
        String type;
        if (api.getInputFactory(ANVIL, String.class).isPresent()) {
            type = ANVIL;
        } else {
            type = CHAT;
            actor.sendTranslatedMessage(Messages.INPUT_BETTERINPUTS_PROMPT, Key.of("prompt", prompt));
        }
        api.createInput(String.class).type(type).actor(actor.getHandle()).param("name", BukkitColor.apply(prompt))
            .cancelListener(
                (provider, reason) -> {
                    consumer.accept(actor, null);
                    actor.sendTranslatedMessage(Messages.INPUT_BETTERINPUTS_CANCELLED, Key.of("reason", reason));
                })
            .exceptionHandler(exp -> actor.sendTranslatedMessage(Messages.INPUT_BETTERINPUTS_FAILED, Key.of("message", exp.getMessage())))
            .provide().withModifier(new AttemptModifier<>(ATTEMPTS,
                (string) -> string.equalsIgnoreCase(CANCEL_MESSAGE) || predicate.test(string), (p) -> actor.sendMessage(retryMessage)))
            .asFuture().thenAccept(str -> {
                if (str.equalsIgnoreCase(CANCEL_MESSAGE)) {
                    consumer.accept(actor, null);
                    actor.sendTranslatedMessage(Messages.INPUT_MANUAL_CANCEL);
                    return;
                }
                consumer.accept(actor, str);
            });
    }

}
