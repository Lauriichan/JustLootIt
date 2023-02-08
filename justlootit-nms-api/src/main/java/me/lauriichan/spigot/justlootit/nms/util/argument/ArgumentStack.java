package me.lauriichan.spigot.justlootit.nms.util.argument;

import java.util.Iterator;
import java.util.Map.Entry;

final class ArgumentStack implements Iterable<Entry<String, Class<?>>> {

    private final Object lock = new Object();

    private Object[][] data;
    private int size = 0;

    private final int expand;

    private void expand() {
        Object[][] newData = new Object[data.length + expand][2];
        System.arraycopy(data, 0, newData, 0, data.length);
        data = newData;
    }

    public ArgumentStack() {
        this(4);
    }

    public ArgumentStack(int expand) {
        this.data = new Object[this.expand = Math.min(Math.abs(expand), 16)][2];
    }

    public ArgumentStack push(String key, Class<?> type) {
        if (size == data.length) {
            synchronized (lock) {
                expand();
            }
        }
        synchronized (lock) {
            int index = size++;
            if (data[index] == null) {
                data[index] = new Object[2];
            }
            data[index][0] = key;
            data[index][1] = type;
        }
        return this;
    }

    public Entry<String, Class<?>> get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " is not valid in range 0 - " + size);
        }
        synchronized (lock) {
            return new StackEntry(data[index]);
        }
    }

    public int size() {
        return size;
    }

    @Override
    public Iterator<Entry<String, Class<?>>> iterator() {
        Entry<String, Class<?>>[] entries;
        synchronized (lock) {
            entries = new StackEntry[size];
            for (int index = 0; index < size; index++) {
                entries[index] = new StackEntry(data[index]);
            }
        }
        return new StackIterator(entries);
    }

    public void throwIfPresent() {
        if (size == 0) {
            return;
        }
        throw new NotEnoughArgumentsException(this);
    }

    private class StackIterator implements Iterator<Entry<String, Class<?>>> {

        private final Entry<String, Class<?>>[] entries;
        private int index = 0;

        public StackIterator(Entry<String, Class<?>>[] entries) {
            this.entries = entries;
        }

        @Override
        public boolean hasNext() {
            return entries.length != index;
        }

        @Override
        public Entry<String, Class<?>> next() {
            return entries[index++];
        }

    }

    private class StackEntry implements Entry<String, Class<?>> {

        private final String key;
        private final Class<?> type;

        public StackEntry(Object[] data) {
            this.key = (String) data[0];
            this.type = (Class<?>) data[1];
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Class<?> getValue() {
            return type;
        }

        @Override
        public Class<?> setValue(Class<?> value) {
            throw new UnsupportedOperationException("setValue is not supported by " + getClass().getTypeName());
        }

    }

}