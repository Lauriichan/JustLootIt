package me.lauriichan.spigot.justlootlit.storage.test.simple.model;

public class SimpleObject {

    public final int[] numbers;

    public SimpleObject(final int number) {
        this.numbers = new int[] {
            number
        };
    }

    public SimpleObject(final int... numbers) {
        this.numbers = numbers;
    }

}