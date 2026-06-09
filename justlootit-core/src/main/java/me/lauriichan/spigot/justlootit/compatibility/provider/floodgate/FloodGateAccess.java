package me.lauriichan.spigot.justlootit.compatibility.provider.floodgate;

import java.util.UUID;

import org.geysermc.floodgate.api.FloodgateApi;

public class FloodGateAccess implements IFloodGateAccess {

    private final FloodgateApi api;

    public FloodGateAccess() {
        api = FloodgateApi.getInstance();
    }

    @Override
    public boolean isBedrockPlayer(UUID uuid) {
        return api.isFloodgatePlayer(uuid);
    }

}
