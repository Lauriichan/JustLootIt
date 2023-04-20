package me.lauriichan.spigot.justlootit.util.color;

import java.util.Iterator;
import java.util.List;

public class ColorString {

    protected final ColorList content = new ColorList();

    public static ColorString of(final String text) {
        return new ColorString().add(text);
    }

    public ColorString() {
        onInit();
    }

    protected void onInit() {}

    public List<String> asColoredList() {
        return content.asColoredList();
    }

    public String asColoredString() {
        return content.asColoredString();
    }

    public String color(final int index) {
        return content.get(index);
    }

    public String plain(final int index) {
        return content.getPlain(index);
    }

    public String[] color(final int start, final int length) {
        final int size = content.size();
        if (start >= size) {
            throw new IndexOutOfBoundsException("start can't be bigger than size (" + Math.abs(size - start) + ")");
        }
        if (length > size) {
            throw new IndexOutOfBoundsException("length can't be bigger than size (" + Math.abs(size - length) + ")");
        }
        final String[] output = new String[length];
        for (int index = start; index < length; index++) {
            output[index] = color(index);
        }
        return output;
    }

    public String[] plain(final int start, final int length) {
        final int size = content.size();
        if (start >= size) {
            throw new IndexOutOfBoundsException("start can't be bigger than size (" + Math.abs(size - start) + ")");
        }
        if (length > size) {
            throw new IndexOutOfBoundsException("length can't be bigger than size (" + Math.abs(size - length) + ")");
        }
        final String[] output = new String[length];
        for (int index = start; index < length; index++) {
            output[index] = plain(index);
        }
        return output;
    }

    public ColorString set(final int index, final String line) {
        content.set(index, line);
        return this;
    }

    public ColorString set(final String... lines) {
        content.clear();
        return add(lines);
    }

    public ColorString set(final List<String> lines) {
        content.clear();
        return add(lines);
    }

    public ColorString set(final Iterable<String> lines) {
        return lines == null ? clear() : set(lines.iterator());
    }

    public ColorString set(final Iterator<String> lines) {
        content.clear();
        return add(lines);
    }

    public ColorString add(final String line) {
        content.add(line);
        return this;
    }

    public ColorString add(final int index, final String line) {
        content.add(index, line);
        return this;
    }

    public ColorString add(final String... lines) {
        if (lines == null) {
            return this;
        }
        for (int index = 0; index < lines.length; index++) {
            content.add(lines[index]);
        }
        return this;
    }

    public ColorString add(final List<String> lines) {
        if (lines == null) {
            return this;
        }
        final int size = lines.size();
        for (int index = 0; index < size; index++) {
            content.add(lines.get(size));
        }
        return this;
    }

    public ColorString add(final Iterable<String> lines) {
        return lines == null ? this : add(lines.iterator());
    }

    public ColorString add(final Iterator<String> lines) {
        if (lines == null) {
            return this;
        }
        while (lines.hasNext()) {
            content.add(lines.next());
        }
        return this;
    }

    public String removeGet(final int index) {
        return content.remove(index);
    }

    public ColorString remove(final int index) {
        content.remove(index);
        return this;
    }

    public ColorString remove(final String line) {
        content.remove(line);
        return this;
    }

    public ColorString remove(final String... lines) {
        if (lines == null) {
            return this;
        }
        for (int index = 0; index < lines.length; index++) {
            content.remove(lines[index]);
        }
        return this;
    }

    public ColorString remove(final List<String> lines) {
        if (lines == null) {
            return this;
        }
        final int size = lines.size();
        for (int index = 0; index < size; index++) {
            content.remove(lines.get(size));
        }
        return this;
    }

    public ColorString remove(final Iterable<String> lines) {
        return lines == null ? this : remove(lines.iterator());
    }

    public ColorString remove(final Iterator<String> lines) {
        if (lines == null) {
            return this;
        }
        while (lines.hasNext()) {
            content.remove(lines.next());
        }
        return this;
    }

    public int length() {
        return content.size();
    }

    public ColorString clear() {
        content.clear();
        return this;
    }

}