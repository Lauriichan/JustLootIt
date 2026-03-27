package me.lauriichan.spigot.justlootit.nms.util;

import java.util.function.Function;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.registry.RegistryAware;

public final class RegistryUtil {

    private static final Function<Keyed, NamespacedKey> GET_KEY_FUNC;

    static {
        Function<Keyed, NamespacedKey> getKeyFunc;
        try {
            RegistryAware.class.getClass();
            getKeyFunc = (keyed) -> {
                if (keyed instanceof RegistryAware regAware) {
                    return regAware.getKeyOrThrow();
                }
                return keyed.getKey();
            };
        } catch (NoClassDefFoundError err) {
            getKeyFunc = Keyed::getKey;
        }
        GET_KEY_FUNC = getKeyFunc;
    }

    private RegistryUtil() {
        throw new UnsupportedOperationException();
    }

    public static NamespacedKey getKey(Keyed keyed) {
        return GET_KEY_FUNC.apply(keyed);
    }

}
