package me.lauriichan.spigot.justlootit.util.attribute;

import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

public abstract class Attributable implements IAttributable {

    private final Object2ObjectArrayMap<String, Object> attributes = new Object2ObjectArrayMap<>();

    @Override
    public final Object attr(String key) {
        return attributes.get(key);
    }

    @Override
    public final <T> T attr(String key, Class<T> type) {
        return attrOrDefault(key, type, null);
    }

    @Override
    public final <T> T attrOrDefault(String key, Class<T> type, T fallback) {
        Object obj = attributes.get(key);
        if (obj == null || !type.isAssignableFrom(obj.getClass())) {
            return fallback;
        }
        return type.cast(obj);
    }

    @Override
    public final boolean attrHas(String key) {
        return attributes.containsKey(key);
    }

    @Override
    public final boolean attrHas(String key, Class<?> type) {
        Object obj = attributes.get(key);
        return obj != null && type.isAssignableFrom(obj.getClass());
    }

    @Override
    public final void attrSet(String key, Object object) {
        if (object == null) {
            attributes.remove(key);
            return;
        }
        attributes.put(key, object);
    }

    @Override
    public final Object attrUnset(String key) {
        return attributes.remove(key);
    }

    @Override
    public <T> T attrUnset(String key, Class<T> type) {
        return attrUnsetOrDefault(key, type, null);
    }

    @Override
    public <T> T attrUnsetOrDefault(String key, Class<T> type, T fallback) {
        Object obj = attributes.remove(key);
        if (obj == null || !type.isAssignableFrom(obj.getClass())) {
            return fallback;
        }
        return type.cast(obj);
    }

    @Override
    public final void attrClear() {
        attributes.clear();
    }

    @Override
    public final int attrAmount() {
        return attributes.size();
    }

    @Override
    public final Set<String> attrKeys() {
        return attributes.keySet();
    }

}
