package me.lauriichan.spigot.justlootit.nms.io;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public final class IOProvider {

    private final Object2ObjectOpenHashMap<Class<?>, IOHandler<?>> handlers = new Object2ObjectOpenHashMap<>();

    public boolean register(final IOHandler<?> handler) {
        if (handler.type() == null || handlers.containsKey(handler.type())) {
            return false;
        }
        handlers.put(handler.type(), handler);
        return true;
    }

    public boolean unregister(final Class<?> type) {
        if (type == null || !handlers.containsKey(type)) {
            return false;
        }
        handlers.remove(type);
        return true;
    }

    public void unregisterAll() {
        handlers.clear();
    }

    public Class<?>[] handlerTypes() {
        return handlers.keySet().toArray(Class[]::new);
    }

    public IOHandler<?> handlerOf(final Class<?> type) {
        if (type == null) {
            return null;
        }
        if (type.isArray()) {
            return searchFor(type.getComponentType());
        }
        return searchFor(type);
    }

    private IOHandler<?> searchFor(final Class<?> type) {
        final IOHandler<?> handler = handlers.get(type);
        if (handler != null) {
            return handler;
        }
        for (final Class<?> current : handlers.keySet()) {
            if (type.isAssignableFrom(current)) {
                return handler;
            }
        }
        return handler;
    }

}
