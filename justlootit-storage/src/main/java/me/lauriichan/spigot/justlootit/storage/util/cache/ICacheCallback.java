package me.lauriichan.spigot.justlootit.storage.util.cache;

public interface ICacheCallback<K, C> {
    
    default void onInvalidate(K key, C value) throws Exception {
        onRemove(key, value);
    }
    
    void onRemove(K key, C value) throws Exception;

}
