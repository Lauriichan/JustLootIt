package me.lauriichan.spigot.justlootit.util.attribute;

import java.util.Collections;
import java.util.Set;

public interface IAttributable {

    default Object attr(final String key) {
        return null;
    }

    default <T> T attr(final String key, final Class<T> type) {
        return null;
    }

    default <T> T attrOrDefault(final String key, final Class<T> type, final T fallback) {
        return fallback;
    }

    default boolean attrHas(final String key) {
        return false;
    }

    default boolean attrHas(final String key, final Class<?> type) {
        return false;
    }

    default void attrSet(final String key, final Object object) {}

    default Object attrUnset(final String key) {
        return null;
    }

    default <T> T attrUnset(final String key, final Class<T> type) {
        return null;
    }

    default <T> T attrUnsetOrDefault(final String key, final Class<T> type, final T fallback) {
        return null;
    }

    default void attrClear() {}

    default int attrAmount() {
        return 0;
    }

    default Set<String> attrKeys() {
        return Collections.emptySet();
    }

}
