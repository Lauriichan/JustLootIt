package me.lauriichan.spigot.justlootit.util;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import it.unimi.dsi.fastutil.doubles.Double2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class WeightedList<E> implements Iterable<WeightedList.Entry<E>> {

    public static class Builder<E> {

        private final WeightedList<E> list = new WeightedList<>();

        public Builder<E> add(double weight, E element) {
            list.add(weight, element);
            return this;
        }

        public WeightedList<E> build() {
            return list;
        }

    }

    public static <E> Builder<E> builder() {
        return new Builder<>();
    }

    public static record Entry<E>(double position, double weight, E element) {}

    private final Double2ObjectRBTreeMap<Entry<E>> sortedElements = new Double2ObjectRBTreeMap<>();
    private final ObjectArrayList<Entry<E>> elements = new ObjectArrayList<>();
    private volatile double totalWeight;

    public final E randomItem(Random random) {
        if (sortedElements.isEmpty()) {
            throw new IllegalStateException("No items available");
        }
        double randomWeight = random.nextDouble() * totalWeight;
        Entry<E> entry = sortedElements.get(randomWeight);
        if (entry != null) {
            return entry.element();
        }
        return sortedElements.get(sortedElements.headMap(randomWeight).lastDoubleKey()).element();
    }

    public final void add(double weight, E element) {
        Optional<Entry<E>> opt = elements.stream().filter(entry -> Objects.equals(entry.element(), element)).findFirst();
        if (opt.isPresent()) {
            totalWeight += weight;
            Entry<E> original = opt.get();
            double total = original.position();
            int start = elements.indexOf(original);
            ObjectArrayList<Entry<E>> list = new ObjectArrayList<>(elements.subList(start, elements.size()));
            elements.removeAll(list);
            list.forEach(e -> sortedElements.remove(e.position()));
            list.remove(0);
            elements.add(original = new Entry<>(total, original.weight() + weight, element));
            sortedElements.put(original.position(), original);
            total += original.weight();
            for (Entry<E> entry : list) {
                entry = new Entry<>(total, entry.weight(), entry.element());
                elements.add(entry);
                sortedElements.put(entry.position(), entry);
                total += entry.weight();
            }
            this.totalWeight = total;
            return;
        }
        Entry<E> entry = new Entry<>(totalWeight, weight, element);
        sortedElements.put(entry.position(), entry);
        elements.add(entry);
        totalWeight += weight;
    }

    public final void remove(E element) {
        Optional<Entry<E>> opt = elements.stream().filter(entry -> Objects.equals(entry.element(), element)).findFirst();
        if (opt.isEmpty()) {
            return;
        }
        int index = elements.indexOf(opt.get());
        elements.remove(index);
        double total = opt.get().position();
        sortedElements.remove(total);
        Entry<E> entry;
        for (int i = index; i < elements.size(); i++) {
            entry = elements.get(i);
            sortedElements.remove(entry.position());
            entry = new Entry<>(total, entry.weight(), entry.element());
            elements.set(i, entry);
            sortedElements.put(total, entry);
            total += entry.weight();
        }
        this.totalWeight = total;
    }

    @Override
    public Iterator<Entry<E>> iterator() {
        return elements.iterator();
    }

}
