package me.lauriichan.spigot.justlootlit.storage.test.simple.model;

import me.lauriichan.spigot.justlootit.storage.Storable;

public class SimpleObject extends Storable {
    
    public final int number;
    
    public SimpleObject(long id, int number) {
        super(id);
        this.number = number;
    }
    
    public SimpleObject withNumber(int number) {
        return new SimpleObject(id, number);
    }

}