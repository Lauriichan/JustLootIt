package me.lauriichan.spigot.justlootit.storage;

public interface IModifiable {
    
    default void unsetDirty() {}

    boolean isDirty();

}
