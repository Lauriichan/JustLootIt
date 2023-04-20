package me.lauriichan.spigot.justlootit;

import org.bukkit.NamespacedKey;

public final class JustLootItKey {

    private JustLootItKey() {
        throw new UnsupportedOperationException();
    }

    private static NamespacedKey identity;

    private static NamespacedKey breakData;

    public static void setup(final JustLootItPlugin plugin) {
        identity = plugin.key("id");
        breakData = plugin.key("break");
    }

    public static NamespacedKey identity() {
        return identity;
    }

    public static NamespacedKey breakData() {
        return breakData;
    }

}
