package me.lauriichan.spigot.justlootit.storage;

public abstract class Storable {

    protected final long id;

    public Storable(final long id) {
        this.id = id;
    }

    public final long id() {
        return id;
    }

}
