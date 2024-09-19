package me.lauriichan.spigot.justlootit;

import org.bukkit.NamespacedKey;

public final class JustLootItKey {

    private JustLootItKey() {
        throw new UnsupportedOperationException();
    }

    private static NamespacedKey identity;
    private static NamespacedKey legacyChestOffset;
    private static NamespacedKey chestOffset;
    private static NamespacedKey breakData;

    public static void setup(final JustLootItPlugin plugin) {
        identity = plugin.key("id");
        legacyChestOffset = plugin.key("chest");
        chestOffset = plugin.key("chest_offset");
        breakData = plugin.key("break");
    }

    public final static NamespacedKey identity() {
        return identity;
    }
    
    public final static NamespacedKey legacyChestOffset() {
        return legacyChestOffset;
    }
    
    public final static NamespacedKey chestOffset() {
        return chestOffset;
    }

    public final static NamespacedKey breakData() {
        return breakData;
    }

}
