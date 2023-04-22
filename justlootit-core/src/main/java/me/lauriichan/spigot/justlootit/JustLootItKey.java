package me.lauriichan.spigot.justlootit;

import org.bukkit.NamespacedKey;

public final class JustLootItKey {

    private JustLootItKey() {
        throw new UnsupportedOperationException();
    }

    private static NamespacedKey identity;
    private static NamespacedKey chestData;
    private static NamespacedKey breakData;

    public static void setup(final JustLootItPlugin plugin) {
        identity = plugin.key("id");
        chestData = plugin.key("chest");
        breakData = plugin.key("break");
    }

    public static NamespacedKey identity() {
        return identity;
    }
    
    public static NamespacedKey chestData() {
        return chestData;
    }

    public static NamespacedKey breakData() {
        return breakData;
    }

}
