package me.lauriichan.spigot.justlootit.nms.v1_21_R2.debug;

import org.bukkit.craftbukkit.v1_21_R2.persistence.CraftPersistentDataContainer;
import org.bukkit.persistence.PersistentDataContainer;

import me.lauriichan.spigot.justlootit.nms.debug.IDebugHelper;

public final class Debug1_21_R2 implements IDebugHelper {

    @Override
    public String persistentDataAsString(PersistentDataContainer container) {
        if (!(container instanceof CraftPersistentDataContainer craftContainer) || craftContainer.isEmpty()) {
            return "";
        }
        return NbtWriter1_21_R2.serialize(craftContainer.toTagCompound(), true);
    }

}
