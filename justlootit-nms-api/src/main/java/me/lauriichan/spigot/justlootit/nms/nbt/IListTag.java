package me.lauriichan.spigot.justlootit.nms.nbt;

public interface IListTag<T> extends Iterable<T> {
    
    TagType<T> type();
    
    int size();
    
    boolean isEmpty();
    
    T get(int index);
    
    T remove(int index);
    
    void add(T value);
    
    void add(int index, T value);
    
    void set(int index, T value);
    
    boolean contains(T value);
    
    void clear();
    
    String asString();

}
