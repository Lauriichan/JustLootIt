package me.lauriichan.spigot.justlootit.input;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

import me.lauriichan.laylib.command.Actor;

public abstract class InputProvider {
    
    public static final String CANCEL_MESSAGE = "#cancel";

    @SuppressWarnings("rawtypes")
    private static final Predicate TRUE = (a) -> true;

    @SuppressWarnings("unchecked")
    public final void getStringInput(Actor<?> actor, String prompt, String retryMessage, BiConsumer<Actor<?>, String> consumer) {
        getStringInput(actor, prompt, retryMessage, TRUE, consumer);
    }

    public abstract void getStringInput(Actor<?> actor, String prompt, String retryMessage, Predicate<String> predicate,
        BiConsumer<Actor<?>, String> consumer);

    @SuppressWarnings("unchecked")
    public final void getLongInput(Actor<?> actor, String prompt, String retryMessage, BiConsumer<Actor<?>, Long> consumer) {
        getLongInput(actor, prompt, retryMessage, TRUE, consumer);
    }

    public void getLongInput(Actor<?> actor, String prompt, String retryMessage, Predicate<Long> predicate,
        BiConsumer<Actor<?>, Long> consumer) {
        getStringInput(actor, prompt, retryMessage, longToString(predicate), longToString(consumer));
    }

    protected final Predicate<String> longToString(Predicate<Long> predicate) {
        return (str) -> {
            try {
                return predicate.test(Long.parseLong(str));
            } catch (NumberFormatException | NullPointerException e) {
                return false;
            }
        };
    }
    
    protected final BiConsumer<Actor<?>, String> longToString(BiConsumer<Actor<?>, Long> consumer) {
        return (actor, value) -> consumer.accept(actor, Long.parseLong(value));
    }
    
}
