package me.lauriichan.spigot.justlootit.util;

import java.util.UUID;

public final class ValueEncoder {
    
    private ValueEncoder() {
        throw new UnsupportedOperationException();
    }

    private static final char[] LONG_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789-_.".toCharArray();
    private static final int BASE = LONG_CHARS.length;
    
    private static final String ZERO = Character.toString(LONG_CHARS[0]);

    public static String encode(UUID id) {
        return encode(id.getLeastSignificantBits()) + encode(id.getMostSignificantBits());
    }
    
    public static String encode(int value) {
        if (value == 0) {
            return ZERO;
        }
        char[] buffer = new char[7];
        int position = 0;
        if (value < 0) {
            value += Integer.MIN_VALUE;
            buffer[position++] = '/';
        }
        while (value != 0) {
            buffer[position++] = LONG_CHARS[(value % BASE)];
            value /= BASE;
        }
        return new String(buffer, 0, position);
    }

    public static String encode(long value) {
        if (value == 0) {
            return ZERO;
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
