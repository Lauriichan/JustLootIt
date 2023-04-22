package me.lauriichan.spigot.justlootit.nms.v1_19_R3.debug;

import org.bukkit.craftbukkit.v1_19_R3.persistence.CraftPersistentDataContainer;
import org.bukkit.persistence.PersistentDataContainer;

import me.lauriichan.spigot.justlootit.nms.debug.IDebugHelper;

public final class Debug1_19_R3 implements IDebugHelper {

    @Override
    public String persistentDataAsString(PersistentDataContainer container) {
        if (!(container instanceof CraftPersistentDataContainer craftContainer) || craftContainer.isEmpty()) {
            return "";
        }
        return NbtWriter1_19_R3.serialize(craftContainer.toTagCompound(), true);
    }

}
