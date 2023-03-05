package me.lauriichan.spigot.justlootit;

import org.bukkit.NamespacedKey;

public final class JustLootItKey {

    private JustLootItKey() {
        throw new UnsupportedOperationException();
    }
    
    private static NamespacedKey identity;
    
    public static void setup(JustLootItPlugin plugin) {
        identity = plugin.key("id");
    }
    
    public static NamespacedKey identity() {
        return identity;
    }
    
}
