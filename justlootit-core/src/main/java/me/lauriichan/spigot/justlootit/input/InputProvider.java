package me.lauriichan.spigot.justlootit.input;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.laylib.command.Actor;

public abstract class InputProvider {

    public static final String CANCEL_MESSAGE = "#cancel";

    private static final ObjectList<String> BOOL_TRUE = ObjectLists.unmodifiable(ObjectArrayList.of("yes", "true", "y", "on"));
    private static final ObjectList<String> BOOL_FALSE = ObjectLists.unmodifiable(ObjectArrayList.of("no", "false", "n", "off"));

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

    public final void getBooleanInput(Actor<?> actor, String prompt, String retryMessage, BiConsumer<Actor<?>, Boolean> consumer) {
        getStringInput(actor, prompt, retryMessage, (str) -> BOOL_TRUE.contains(str = str.toLowerCase()) || BOOL_FALSE.contains(str),
            (act, value) -> consumer.accept(act, value == null ? null : BOOL_TRUE.contains(value.toLowerCase())));
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
        return (actor, value) -> consumer.accept(actor, value == null ? null : Long.parseLong(value));
    }

}
