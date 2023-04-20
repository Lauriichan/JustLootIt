package me.lauriichan.spigot.justlootit.message.component;

import java.awt.Color;
import java.util.ArrayList;

import me.lauriichan.spigot.justlootit.util.color.ColorParser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public final class ComponentParser {

    public static final Color DEFAULT_COLOR = ColorParser.parse("#AAAAAA");

    private ComponentParser() {
        throw new UnsupportedOperationException();
    }

    public static BaseComponent[] parse(final String richString) {
        return parse(richString, DEFAULT_COLOR, null, null);
    }

    public static BaseComponent[] parse(final String richString, final ChatColor defaultColor) {
        return parse(richString, defaultColor == null || defaultColor.getColor() == null ? DEFAULT_COLOR : defaultColor.getColor(), null,
            null);
    }

    public static BaseComponent[] parse(final String richString, final Color defaultColor) {
        return parse(richString, defaultColor == null ? DEFAULT_COLOR : defaultColor, null, null);
    }

    public static BaseComponent[] parse(final String richString, final HoverEvent hover) {
        return parse(richString, DEFAULT_COLOR, null, hover);
    }

    public static BaseComponent[] parse(final String richString, final ChatColor defaultColor, final HoverEvent hover) {
        return parse(richString, defaultColor == null || defaultColor.getColor() == null ? DEFAULT_COLOR : defaultColor.getColor(), null,
            hover);
    }

    public static BaseComponent[] parse(final String richString, final Color defaultColor, final HoverEvent hover) {
        return parse(richString, defaultColor == null ? DEFAULT_COLOR : defaultColor, null, hover);
    }

    public static BaseComponent[] parse(final String richString, final ClickEvent click) {
        return parse(richString, DEFAULT_COLOR, click, null);
    }

    public static BaseComponent[] parse(final String richString, final ChatColor defaultColor, final ClickEvent click) {
        return parse(richString, defaultColor == null || defaultColor.getColor() == null ? DEFAULT_COLOR : defaultColor.getColor(), click,
            null);
    }

    public static BaseComponent[] parse(final String richString, final Color defaultColor, final ClickEvent click) {
        return parse(richString, defaultColor == null ? DEFAULT_COLOR : defaultColor, click, null);
    }

    public static BaseComponent[] parse(final String richString, final ClickEvent click, final HoverEvent hover) {
        return parse(richString, DEFAULT_COLOR, click, hover);
    }

    public static BaseComponent[] parse(final String richString, final ChatColor defaultColor, final ClickEvent click,
        final HoverEvent hover) {
        return parse(richString, defaultColor == null || defaultColor.getColor() == null ? DEFAULT_COLOR : defaultColor.getColor(), click,
            hover);
    }

    public static BaseComponent[] parse(final String richString, final Color defaultColor, final ClickEvent click, final HoverEvent hover) {
        if (richString == null || richString.isBlank()) {
            return null;
        }
        final ArrayList<BaseComponent> array = new ArrayList<>();
        TextComponent component = new TextComponent();
        final ChatColor colorDefault = ChatColor.of(defaultColor);
        ChatColor color = colorDefault;
        component.setColor(color);
        component.setClickEvent(click);
        component.setHoverEvent(hover);
        StringBuilder builder = new StringBuilder();
        final int length = richString.length();
        for (int index = 0; index < length; index++) {
            final char chr = richString.charAt(index);
            if (chr == '&') {
                if (index + 1 >= length) {
                    break;
                }
                if (richString.charAt(index + 1) != '#' || index + 4 >= length) {
                    final ChatColor format = ChatColor.getByChar(richString.charAt(index + 1));
                    if (format == null) {
                        builder.append(chr);
                        continue;
                    }
                    index += 1;
                    if (builder.length() > 0) {
                        final TextComponent old = component;
                        component = new TextComponent();
                        component.copyFormatting(old, true);
                        old.setText(builder.toString());
                        builder = new StringBuilder();
                        array.add(old);
                    }
                    if (format.getColor() == null) {
                        if (format == ChatColor.MAGIC) {
                            component.setObfuscated(true);
                            continue;
                        }
                        if (format == ChatColor.STRIKETHROUGH) {
                            component.setStrikethrough(true);
                            continue;
                        }
                        if (format == ChatColor.UNDERLINE) {
                            component.setUnderlined(true);
                            continue;
                        }
                        if (format == ChatColor.BOLD) {
                            component.setBold(true);
                            continue;
                        }
                        if (format == ChatColor.ITALIC) {
                            component.setItalic(true);
                            continue;
                        }
                        component.setBold(false);
                        component.setStrikethrough(false);
                        component.setObfuscated(false);
                        component.setItalic(false);
                        component.setUnderlined(false);
                        component.setColor(colorDefault);
                        continue;
                    }
                    component = new TextComponent();
                    component.setClickEvent(click);
                    component.setHoverEvent(hover);
                    component.setColor(format);
                    continue;
                }
                final StringBuilder hex = new StringBuilder();
                for (int idx = index + 2; idx <= index + 7; idx++) {
                    final char hch = richString.charAt(idx);
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
                    builder.append(chr);
                    continue;
                }
                if (hex.length() == 3) {
                    index -= 3;
                }
                index += 7;
                if (builder.length() > 0) {
                    final TextComponent old = component;
                    component = new TextComponent();
                    component.copyFormatting(old, true);
                    old.setText(builder.toString());
                    builder = new StringBuilder();
                    array.add(old);
                }
                color = ChatColor.of(ColorParser.parse(hex.toString()));
                component.setColor(color);
                continue;
            }
            builder.append(chr);
        }
        if (builder.length() > 0) {
            component.setText(builder.toString());
            array.add(component);
        }
        return array.toArray(BaseComponent[]::new);
    }

}