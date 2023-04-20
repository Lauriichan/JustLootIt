package me.lauriichan.spigot.justlootit.util.attribute;

import java.util.Collections;
import java.util.Set;

public interface IAttributable {

    default Object attr(String key) {
        return null;
    }

    default <T> T attr(String key, Class<T> type) {
        return null;
    }

    default <T> T attrOrDefault(String key, Class<T> type, T fallback) {
        return fallback;
    }

    default boolean attrHas(String key) {
        return false;
    }

    default boolean attrHas(String key, Class<?> type) {
        return false;
    }

    default void attrSet(String key, Object object) {}

    default Object attrUnset(String key) {
        return null;
    }
    
    default <T> T attrUnset(String key, Class<T> type) {
        return null;
    }
    
    default <T> T attrUnsetOrDefault(String key, Class<T> type, T fallback) {
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
