package me.lauriichan.spigot.justlootit.util.color;

import java.awt.Color;

public final class ColorParser {

    private ColorParser() {}

    public static String asString(final Color color) {
        return '#' + Integer.toString(color.getRed(), 16) + Integer.toString(color.getGreen(), 16) + Integer.toString(color.getBlue(), 16);
    }

    public static Color parse(final String hex) {
        if (hex.startsWith("#")) {
            return parse(hex.substring(1));
        }
        final int length = hex.length();
        int red = 0, green = 0, blue = 0;
        switch (length) {
        case 3:
            red = parseHexTwice(hex.substring(0, 1));
            green = parseHexTwice(hex.substring(1, 2));
            blue = parseHexTwice(hex.substring(2, 3));
            break;
        case 6:
            red = parseHex(hex.substring(0, 2));
            green = parseHex(hex.substring(2, 4));
            blue = parseHex(hex.substring(4, 6));
            break;
        }
        return new Color(red, green, blue);
    }

    public static Color parseOrNull(final String hex) {
        if (hex.startsWith("#")) {
            return parseOrNull(hex.substring(1));
        }
        final int length = hex.length();
        int red, green, blue;
        try {
            switch (length) {
            case 3:
                red = parseHexTwiceThrow(hex.substring(0, 1));
                green = parseHexTwiceThrow(hex.substring(1, 2));
                blue = parseHexTwiceThrow(hex.substring(2, 3));
                break;
            case 6:
                red = parseHexThrow(hex.substring(0, 2));
                green = parseHexThrow(hex.substring(2, 4));
                blue = parseHexThrow(hex.substring(4, 6));
                break;
            default:
                return null;
            }
        } catch (final NumberFormatException ignore) {
            return null;
        }
        return new Color(red, green, blue);
    }

    private static int parseHexTwiceThrow(final String value) throws NumberFormatException {
        return parseHexThrow(value + value);
    }

    private static int parseHexTwice(final String value) {
        return parseHex(value + value);
    }

    private static int parseHexThrow(final String value) throws NumberFormatException {
        return Integer.parseInt(value, 16);
    }

    private static int parseHex(final String value) {
        try {
            return Integer.parseInt(value, 16);
        } catch (final NumberFormatException ignore) {
            return 0;
        }
    }

}