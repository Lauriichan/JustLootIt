package me.lauriichan.spigot.justlootit.input;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.spigot.justlootit.message.Messages;

public class SimpleChatInputProvider extends InputProvider {

    public static record ChatInput(SimpleChatInputProvider provider, Actor<?> actor, String retryMessage, Predicate<String> predicate,
        BiConsumer<Actor<?>, String> consumer) {

        public void remove() {
            provider.inputs.remove(actor.getId());
        }

    }
    
    public static final SimpleChatInputProvider CHAT = new SimpleChatInputProvider();
    
    private SimpleChatInputProvider() {}

    private final Object2ObjectArrayMap<UUID, ChatInput> inputs = new Object2ObjectArrayMap<>();

    @Override
    public void getStringInput(Actor<?> actor, String prompt, String retryMessage, Predicate<String> predicate,
        BiConsumer<Actor<?>, String> consumer) {
        actor.sendTranslatedMessage(Messages.INPUT_SIMPLE_PROMPT, Key.of("prompt", prompt));
        inputs.put(actor.getId(), new ChatInput(this, actor, retryMessage, predicate, consumer));
    }
    
    public boolean hasNoInputs() {
        return inputs.isEmpty();
    }
    
    public ChatInput get(UUID id) {
        return inputs.get(id);
    }

}
