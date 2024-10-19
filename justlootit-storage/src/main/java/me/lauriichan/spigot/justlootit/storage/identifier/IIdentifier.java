package me.lauriichan.spigot.justlootit.storage.identifier;

public interface IIdentifier {
    
    long nextId();
    
    void reset();
    
    void delete(long id);
    
    default long lastSaved() {
        return 0L;
    }
    
    default void load() {}
    
    default void save() {}

}
