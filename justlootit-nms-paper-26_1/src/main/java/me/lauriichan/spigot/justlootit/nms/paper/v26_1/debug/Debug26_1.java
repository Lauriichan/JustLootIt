package me.lauriichan.spigot.justlootit.nms.paper.v26_1.debug;

import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.persistence.PersistentDataContainer;

import me.lauriichan.spigot.justlootit.nms.debug.IDebugHelper;

public final class Debug26_1 implements IDebugHelper {

    @Override
    public String persistentDataAsString(PersistentDataContainer container) {
        if (!(container instanceof CraftPersistentDataContainer craftContainer) || craftContainer.isEmpty()) {
            return "";
        }
        return NbtWriter26_1.serialize(craftContainer.toTagCompound(), true);
    }

}
