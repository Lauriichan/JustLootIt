package me.lauriichan.spigot.justlootit;

import java.util.UUID;

import org.bukkit.NamespacedKey;

import me.lauriichan.spigot.justlootit.util.ValueEncoder;

public final class JustLootItKey {

    private JustLootItKey() {
        throw new UnsupportedOperationException();
    }

    public static final String NAMESPACE = "justlootit";

    public static final byte TRUE = (byte) 1;
    public static final byte FALSE = (byte) 0;

    public static final NamespacedKey IDENTITY = key("i");

    public static NamespacedKey keyOf(UUID id) {
        return key(ValueEncoder.encode(id));
    }

    @SuppressWarnings("deprecation")
    private static NamespacedKey key(String key) {
        return new NamespacedKey(NAMESPACE, key);
    }

    public static void main(String[] args) {
        UUID id = UUID.randomUUID();
        System.out.println(ValueEncoder.encode(id));
        System.out.println(id);
        System.out.println(id.toString().replace("-", ""));
    }
    
}
