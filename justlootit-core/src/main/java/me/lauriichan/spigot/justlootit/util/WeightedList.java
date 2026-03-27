package me.lauriichan.spigot.justlootit.util;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import it.unimi.dsi.fastutil.doubles.Double2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class WeightedList<E> {

    private static record Entry<E>(double position, double weight, E element) {}

    private final Double2ObjectRBTreeMap<Entry<E>> sortedElements = new Double2ObjectRBTreeMap<>();
    private final ObjectArrayList<Entry<E>> elements = new ObjectArrayList<>();
    private volatile float totalWeight;

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
            Entry<E> entry = opt.get();
            
            return;
        }
        Entry<E> entry = new Entry<>(totalWeight, weight, element);
        sortedElements.put(entry.position(), entry);
        totalWeight += weight;
    }

    public final void remove(E element) {

    }

}
