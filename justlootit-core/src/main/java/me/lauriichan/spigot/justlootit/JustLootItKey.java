package me.lauriichan.spigot.justlootit;

import java.util.UUID;

import org.bukkit.NamespacedKey;

public final class JustLootItKey {

    private JustLootItKey() {
        throw new UnsupportedOperationException();
    }

    private static final char[] LONG_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789-_.".toCharArray();
    private static final int BASE = LONG_CHARS.length;

    public static final String NAMESPACE = "justlootit";

    public static final byte TRUE = (byte) 1;
    public static final byte FALSE = (byte) 0;

    public static final NamespacedKey IDENTITY = key("i");

    public static NamespacedKey keyOf(UUID id) {
        return key(encode(id));
    }

    @SuppressWarnings("deprecation")
    private static NamespacedKey key(String key) {
        return new NamespacedKey(NAMESPACE, key);
    }

    private static String encode(UUID id) {
        return encode(id.getLeastSignificantBits()) + encode(id.getMostSignificantBits());
    }

    private static String encode(long value) {
        if (value == 0) {
            return "//";
        }
        char[] buffer = new char[13];
        int position = 0;
        if (value < 0) {
            value += Long.MIN_VALUE;
            buffer[position++] = '/';
        }
        while (value != 0) {
            buffer[position++] = LONG_CHARS[(int) (value % BASE)];
            value /= BASE;
        }
        return new String(buffer, 0, position);
    }

}
