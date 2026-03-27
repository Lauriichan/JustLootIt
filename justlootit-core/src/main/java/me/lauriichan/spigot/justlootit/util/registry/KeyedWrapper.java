package me.lauriichan.spigot.justlootit.util.registry;

import java.util.Objects;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.plugin.Plugin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

public class KeyedWrapper<T extends Keyed> {

    public static <T extends Keyed> KeyedWrapper<T> load(Plugin plugin, Registry<T> registry, String... ids) {
        ObjectArrayList<T> values = new ObjectArrayList<>();
        T value;
        for (String id : ids) {
            if (!id.contains(":")) {
                value = registry.get(NamespacedKey.minecraft(id));
            } else {
                value = registry.get(NamespacedKey.fromString(id, plugin));
            }
            if (value == null) {
                continue;
            }
            values.add(value);
        }
        return new KeyedWrapper<>(values.isEmpty() ? ObjectLists.emptyList() : ObjectLists.unmodifiable(values));
    }

    protected final ObjectList<T> valid;

    protected KeyedWrapper(ObjectList<T> valid) {
        this.valid = Objects.requireNonNull(valid);
    }

    public boolean isValue(T value) {
        for (T val : valid) {
            if (val == value) {
                return true;
            }
        }
        return false;
    }

    public final boolean hasValue() {
        return !valid.isEmpty();
    }

    public final int count() {
        return valid.size();
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
