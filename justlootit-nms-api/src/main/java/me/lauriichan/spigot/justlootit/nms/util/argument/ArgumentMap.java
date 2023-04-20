package me.lauriichan.spigot.justlootit.nms.util.argument;

import java.util.HashMap;
import java.util.Objects;

import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.spigot.justlootit.nms.util.Option;

public final class ArgumentMap {

    private final HashMap<String, Object> map = new HashMap<>();
    private final ArgumentStack stack = new ArgumentStack();

    public void throwIfMissing() throws NotEnoughArgumentsException {
        stack.throwIfPresent();
    }

    private String test(final String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("String key can't be null or empty!");
        }
        return key;
    }

    public boolean has(final String key) {
        return map.containsKey(test(key));
    }

    public boolean has(final String key, final Class<?> type) {
        Objects.requireNonNull(type, "Class type can't be null");
        final Object object = map.get(test(key));
        return object != null && type.isInstance(object);
    }

    public Option<Object> get(final String key) {
        return Option.of(map.get(test(key)));
    }

    public <E> Option<E> get(final String key, final Class<E> type) {
        Objects.requireNonNull(type, "Class type can't be null");
        return get(key).filter(object -> type.isAssignableFrom(ClassUtil.toComplexType(object.getClass()))).map(type::cast);
    }

    public Option<Class<?>> getClass(final String key) {
        return Option.of(map.get(test(key))).filter(val -> val instanceof Class).map(val -> (Class<?>) val);
    }

    public <E> Option<Class<? extends E>> getClass(final String key, final Class<E> abstraction) {
        Objects.requireNonNull(abstraction, "Class abstraction can't be null");
        return getClass(key).filter(clazz -> abstraction.isAssignableFrom(ClassUtil.toComplexType(clazz)))
            .map(clazz -> clazz.asSubclass(abstraction));
    }

    public ArgumentMap set(final String key, final Object value) {
        map.put(test(key), Objects.requireNonNull(value));
        return this;
    }

    public ArgumentMap remove(final String key) {
        map.remove(test(key));
        return this;
    }

    public ArgumentMap clear() {
        map.clear();
        return this;
    }

    @Override
    public ArgumentMap clone() {
        final ArgumentMap clone = new ArgumentMap();
        map.putAll(map);
        return clone;
    }

    public void copyFrom(final ArgumentMap map) {
        if (map == null) {
            this.map.clear();
            return;
        }
        this.map.putAll(map.map);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public int size() {
        return map.size();
    }

    public Object require(final String key) {
        return get(key).orElseRun(() -> stack.push(key, Object.class));
    }

    public <E> E require(final String key, final Class<E> type) {
        return get(key, type).orElseRun(() -> stack.push(key, type));
    }

    public Class<?> requireClass(final String key) {
        return getClass(key).orElseRun(() -> stack.push(key, Class.class));
    }

    public <E> Class<? extends E> requireClass(final String key, final Class<E> abstraction) {
        return getClass(key, abstraction).orElseRun(() -> stack.push(key, abstraction.getClass()));
    }

}
