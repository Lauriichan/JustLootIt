package me.lauriichan.spigot.justlootit.nms.v1_20_R4.debug;

import org.bukkit.craftbukkit.v1_20_R4.persistence.CraftPersistentDataContainer;
import org.bukkit.persistence.PersistentDataContainer;

import me.lauriichan.spigot.justlootit.nms.debug.IDebugHelper;

public final class Debug1_20_R4 implements IDebugHelper {

    @Override
    public String persistentDataAsString(PersistentDataContainer container) {
        if (!(container instanceof CraftPersistentDataContainer craftContainer) || craftContainer.isEmpty()) {
            return "";
        }
        return NbtWriter1_20_R4.serialize(craftContainer.toTagCompound(), true);
    }

}
