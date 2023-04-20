package me.lauriichan.spigot.justlootit.util.color;

import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatColor;

public final class BukkitColor {

    private BukkitColor() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final char COLOR_CHAR = '\u00A7';
    public static final char HEX_CHAR = '\u0023';
    public static final char REPLACEMENT_CHAR = '\u0026';
    public static final String ALL_CODES = "0123456789AabCcDdEeFfKkLlMmNnOoRrXx";
    public static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf(COLOR_CHAR) + "[0-9A-FK-ORX]");
    public static final Pattern STRIP_UNCOLORED_PATTERN = Pattern
        .compile("(?i)" + String.valueOf(REPLACEMENT_CHAR) + "([0-9A-FK-ORX]|" + String.valueOf(HEX_CHAR) + "([0-9A-F]{6}|[0-9A-F]{3}))");

    public static String strip(final String text) {
        return text == null ? null : STRIP_COLOR_PATTERN.matcher(text).replaceAll("");
    }

    public static String stripPlain(final String text) {
        return text == null ? null : STRIP_UNCOLORED_PATTERN.matcher(text).replaceAll("");
    }

    public static String apply(final String string) {
        final StringBuilder output = new StringBuilder();
        final int length = string.length();
        for (int index = 0; index < length; index++) {
            final char chr = string.charAt(index);
            if (chr == REPLACEMENT_CHAR) {
                if (index + 1 >= length) {
                    break;
                }
                if (string.charAt(index + 1) != HEX_CHAR || index + 4 >= length) {
                    final ChatColor format = ChatColor.getByChar(string.charAt(index + 1));
                    if (format == null) {
                        output.append(chr);
                        continue;
                    }
                    index++;
                    output.append(format.toString().toLowerCase());
                    continue;
                }
                final StringBuilder hex = new StringBuilder();
                for (int idx = index + 2; idx <= index + 7; idx++) {
                    final char hch = string.charAt(idx);
                    if (hch >= 'A' && hch <= 'Z') {
                        hex.append((char) (hch + 32));
                        continue;
                    }
                    if (hch >= 'a' && hch <= 'z' || hch >= '0' && hch <= '9') {
                        hex.append(hch);
                        continue;
                    }
                    break;
                }
                if (((hex.length() != 6) && (hex.length() != 3))) {
                    output.append(chr);
                    continue;
                }
                if (hex.length() == 3) {
                    index -= 3;
                }
                index += 7;
                output.append(ChatColor.of(ColorParser.parse(hex.toString())).toString().toLowerCase());
                continue;
            }
            output.append(chr);
        }
        return output.toString();
    }

    public static String unapply(final String string) {
        final StringBuilder output = new StringBuilder();
        final int length = string.length();
        for (int index = 0; index < length; index++) {
            final char chr = string.charAt(index);
            if (chr == COLOR_CHAR) {
                if (index + 1 >= length) {
                    break;
                }
                final char next = Character.toLowerCase(string.charAt(index + 1));
                if (next != 'x') {
                    if (ALL_CODES.indexOf(next) == -1) {
                        output.append(chr);
                        continue;
                    }
                    index++;
                    output.append(REPLACEMENT_CHAR).append(next);
                    continue;
                }
                final StringBuilder hex = new StringBuilder();
                boolean colChar = false;
                for (int idx = index + 2; idx <= index + 13; idx++) {
                    final char hch = string.charAt(idx);
                    if (hch == COLOR_CHAR) {
                        colChar = true;
                        continue;
                    }
                    if (!colChar) {
                        break;
                    }
                    if (hch >= 'A' && hch <= 'Z') {
                        hex.append((char) (hch + 32));
                        continue;
                    }
                    if (hch >= 'a' && hch <= 'z' || hch >= '0' && hch <= '9') {
                        hex.append(hch);
                        continue;
                    }
                    break;
                }
                if (((hex.length() != 6) && (hex.length() != 3))) {
                    output.append(chr);
                    continue;
                }
                if (hex.length() == 3) {
                    index -= 6;
                }
                index += 13;
                output.append(REPLACEMENT_CHAR).append(HEX_CHAR).append(hex.toString().toLowerCase());
                continue;
            }
            output.append(chr);
        }
        return output.toString();
    }
}