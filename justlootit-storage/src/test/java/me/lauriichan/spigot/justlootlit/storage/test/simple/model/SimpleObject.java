package me.lauriichan.spigot.justlootlit.storage.test.simple.model;

import me.lauriichan.spigot.justlootit.storage.Storable;

public class SimpleObject extends Storable {

    public final int[] numbers;

    public SimpleObject(final long id, final int number) {
        super(id);
        this.numbers = new int[] {
            number
        };
    }

    public SimpleObject(final long id, final int... numbers) {
        super(id);
        this.numbers = numbers;
    }

    public SimpleObject withNumber(final int number) {
        return new SimpleObject(id, number);
    }

    public SimpleObject withNumbers(final int... numbers) {
        return new SimpleObject(id, numbers);
    }

}