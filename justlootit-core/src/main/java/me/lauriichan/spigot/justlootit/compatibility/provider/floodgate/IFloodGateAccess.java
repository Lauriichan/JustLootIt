package me.lauriichan.spigot.justlootit.compatibility.provider.floodgate;

import java.util.UUID;

public interface IFloodGateAccess {
    
    default boolean isBedrockPlayer(UUID uuid) {
        return false;
    }

}
