package me.lauriichan.spigot.justlootit.compatibility.provider.floodgate;

import java.util.UUID;

import me.lauriichan.spigot.justlootit.compatibility.provider.CompatDependency;

public final class FloodGateHelper {

    private FloodGateHelper() {
        throw new UnsupportedOperationException();
    }

    public static boolean isBedrockPlayer(UUID uuid) {
        IFloodGateProvider provider = CompatDependency.getActiveProvider("floodgate", IFloodGateProvider.class);
        if (provider == null) {
            return false;
        }
        IFloodGateAccess access = provider.access();
        if (access == null) {
            return false;
        }
        return access.isBedrockPlayer(uuid);
    }

}
