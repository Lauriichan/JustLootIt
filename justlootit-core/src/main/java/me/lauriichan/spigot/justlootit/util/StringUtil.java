package me.lauriichan.spigot.justlootit.util;

public final class StringUtil {

    private StringUtil() {
        throw new UnsupportedOperationException();
    }

    public static String formatPascalCase(final String string) {
        final String[] parts = string.split(" ");
        final StringBuilder builder = new StringBuilder();
        for (int index = 0; index < parts.length; index++) {
            if (index != 0) {
                builder.append(' ');
            }
            final String part = parts[index];
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase());
        }
        return builder.toString();
    }
}