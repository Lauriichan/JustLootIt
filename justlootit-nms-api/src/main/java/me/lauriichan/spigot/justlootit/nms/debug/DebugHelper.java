package me.lauriichan.spigot.justlootit.nms.debug;

import org.bukkit.persistence.PersistentDataContainer;

public final class DebugHelper implements IDebugHelper {

    private final IDebugHelper helper;
    private final boolean invalid;

    public DebugHelper(final IDebugHelper helper) {
        this.helper = helper;
        this.invalid = helper == null;
    }

    @Override
    public boolean isValid() {
        return !invalid;
    }

    @Override
    public String persistentDataAsString(PersistentDataContainer container) {
        if (invalid) {
            return "";
        }
        try {
            return helper.persistentDataAsString(container);
        } catch (UnsupportedOperationException ignore) {
            return "";
        }
    }

}
