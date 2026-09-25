package me.lauriichan.spigot.justlootit.util.registry;

import java.util.Objects;

import org.bukkit.Keyed;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

public class EnumWrapper<T extends Keyed> {

    public static <T extends Enum<T> & Keyed> EnumWrapper<T> of(@SuppressWarnings("unchecked") T... values) {
        if (values == null) {
            return new EnumWrapper<>(ObjectLists.emptyList());
        }
        ObjectArrayList<T> valid = new ObjectArrayList<>(values.length);
        for (T value : values) {
            if (value == null || valid.contains(value)) {
                continue;
            }
            valid.add(value);
        }
        if (valid.isEmpty()) {
            return new EnumWrapper<>(ObjectLists.emptyList());
        }
        valid.trim();
        return new EnumWrapper<>(ObjectLists.unmodifiable(valid));
    }

    protected final ObjectList<T> valid;

    protected EnumWrapper(ObjectList<T> valid) {
        this.valid = Objects.requireNonNull(valid);
    }

    public boolean isValue(T value) {
        return valid.contains(value);
    }

    public final boolean hasValue() {
        return !valid.isEmpty();
    }

    public final int count() {
        return valid.size();
    }

    public final ObjectList<T> values() {
        return valid;
    }

    public final T get(int index) {
        if (valid.size() >= index || index < 0) {
            return defaultValue();
        }
        return valid.get(index);
    }

    protected T defaultValue() {
        return null;
    }

}
