package me.lauriichan.spigot.justlootlit.storage.test.simple.model;

import me.lauriichan.spigot.justlootit.storage.Storable;

public class SimpleObject extends Storable {
    
    public final int number;

    public SimpleObject(long id) {
        this(id, (int) Math.round(Math.random() * Integer.MAX_VALUE));
    }
    
    public SimpleObject(long id, int number) {
        super(id);
        this.number = number;
    }

}