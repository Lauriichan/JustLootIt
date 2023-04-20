package me.lauriichan.spigot.justlootit.util.color;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ColorList extends ArrayList<String> {

    private static final long serialVersionUID = 1310623158005774539L;

    public static ColorList convert(final List<String> input) {
        if (input instanceof ColorList) {
            return (ColorList) input;
        }
        final ColorList list = new ColorList();
        final int size = input.size();
        for (int index = 0; index < size; index++) {
            list.add(input.get(index));
        }
        return list;
    }

    public List<String> asColoredList() {
        final ArrayList<String> list = new ArrayList<>();
        final int size = size();
        for (int index = 0; index < size; index++) {
            list.add(get(index));
        }
        return list;
    }

    public List<String> asPlainList() {
        final ArrayList<String> list = new ArrayList<>();
        final int size = size();
        for (int index = 0; index < size; index++) {
            list.add(getPlain(index));
        }
        return list;
    }

    public String asColoredString() {
        final StringBuilder builder = new StringBuilder();
        final int size = size();
        for (int index = 0; index < size; index++) {
            builder.append(get(index));
        }
        return builder.toString();
    }

    public String asPlainString() {
        final StringBuilder builder = new StringBuilder();
        final int size = size();
        for (int index = 0; index < size; index++) {
            builder.append(getPlain(index));
        }
        return builder.toString();
    }

    @Override
    public String get(final int index) {
        return color(super.get(index));
    }

    public String getPlain(final int index) {
        return super.get(index);
    }

    public String getStripped(final int index) {
        return strip(super.get(index));
    }

    @Override
    public boolean addAll(final Collection<? extends String> collection) {
        if (collection == null) {
            return false;
        }
        return super.addAll(collection);
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends String> collection) {
        if (collection == null) {
            return false;
        }
        return super.addAll(index, collection);
    }

    @Override
    public String set(final int index, final String element) {
        if (element == null) {
            return element;
        }
        return super.set(index, uncolor(element));
    }

    @Override
    public boolean add(final String element) {
        if (element == null) {
            return false;
        }
        return super.add(uncolor(element));
    }

    @Override
    public void add(final int index, final String element) {
        if (element == null) {
            return;
        }
        super.add(index, uncolor(element));
    }

    @Override
    public boolean remove(final Object object) {
        if (!(object instanceof String)) {
            return false;
        }
        return super.remove(object);
    }

    @Override
    public boolean contains(final Object object) {
        if (!(object instanceof String)) {
            return false;
        }
        return super.contains(object);
    }

    private String color(final String msg) {
        return BukkitColor.apply(msg);
    }

    private String uncolor(final String msg) {
        return BukkitColor.unapply(msg);
    }

    private String strip(final String msg) {
        return BukkitColor.stripPlain(msg);
    }

}