package me.lauriichan.spigot.justlootit.nms.debug;

import org.bukkit.persistence.PersistentDataContainer;

public interface IDebugHelper {

    default boolean isValid() {
        return true;
    }

    default String persistentDataAsString(PersistentDataContainer container) {
        throw new UnsupportedOperationException();
    }

}
