package me.lauriichan.spigot.justlootit.inventory.handler.item;

import java.util.Iterator;
import java.util.List;

import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.spigot.justlootit.util.color.ColorList;

public abstract class ColoredStringEditor<S extends ColoredStringEditor<S>> {

    protected final ItemEditor editor;
    protected final ColorList content = new ColorList();

    @SuppressWarnings("unchecked")
    protected final S self = (S) this;

    public ColoredStringEditor(final ItemEditor editor) {
        this.editor = editor;
    }

    public String color(final int index) {
        return content.get(index);
    }

    public String plain(final int index) {
        return content.getPlain(index);
    }

    public String stripped(final int index) {
        return content.getStripped(index);
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

    public String[] stripped(final int start, final int length) {
        final int size = content.size();
        if (start >= size) {
            throw new IndexOutOfBoundsException("start can't be bigger than size (" + Math.abs(size - start) + ")");
        }
        if (length > size) {
            throw new IndexOutOfBoundsException("length can't be bigger than size (" + Math.abs(size - length) + ")");
        }
        final String[] output = new String[length];
        for (int index = start; index < length; index++) {
            output[index] = stripped(index);
        }
        return output;
    }

    public S set(final int index, final String line) {
        content.set(index, line);
        return self;
    }

    public S set(final String... lines) {
        content.clear();
        return add(lines);
    }

    public S set(final List<String> lines) {
        content.clear();
        return add(lines);
    }

    public S set(final Iterable<String> lines) {
        return lines == null ? clear() : set(lines.iterator());
    }

    public S set(final Iterator<String> lines) {
        content.clear();
        return add(lines);
    }

    public S add(final String line) {
        content.add(line);
        return self;
    }

    public S add(final int index, final String line) {
        content.add(index, line);
        return self;
    }

    public S add(final String... lines) {
        if (lines == null) {
            return self;
        }
        for (int index = 0; index < lines.length; index++) {
            content.add(lines[index]);
        }
        return self;
    }

    public S add(final List<String> lines) {
        if (lines == null) {
            return self;
        }
        final int size = lines.size();
        for (int index = 0; index < size; index++) {
            content.add(lines.get(index));
        }
        return self;
    }

    public S add(final Iterable<String> lines) {
        return lines == null ? self : add(lines.iterator());
    }

    public S add(final Iterator<String> lines) {
        if (lines == null) {
            return self;
        }
        while (lines.hasNext()) {
            content.add(lines.next());
        }
        return self;
    }

    public String removeGet(final int index) {
        return content.remove(index);
    }

    public S remove(final int index) {
        content.remove(index);
        return self;
    }

    public S remove(final String line) {
        content.remove(line);
        return self;
    }

    public S remove(final String... lines) {
        if (lines == null) {
            return self;
        }
        for (int index = 0; index < lines.length; index++) {
            content.remove(lines[index]);
        }
        return self;
    }

    public S remove(final List<String> lines) {
        if (lines == null) {
            return self;
        }
        final int size = lines.size();
        for (int index = 0; index < size; index++) {
            content.remove(lines.get(size));
        }
        return self;
    }

    public S remove(final Iterable<String> lines) {
        return lines == null ? self : remove(lines.iterator());
    }

    public S remove(final Iterator<String> lines) {
        if (lines == null) {
            return self;
        }
        while (lines.hasNext()) {
            content.remove(lines.next());
        }
        return self;
    }

    public int length() {
        return content.size();
    }

    public S clear() {
        content.clear();
        return self;
    }

    public abstract ItemEditor apply();

    public ItemEditor getHandle() {
        return editor;
    }

}
