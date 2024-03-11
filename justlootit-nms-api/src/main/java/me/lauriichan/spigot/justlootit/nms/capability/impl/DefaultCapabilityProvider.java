package me.lauriichan.spigot.justlootit.nms.capability.impl;

import java.util.List;

import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.capability.Capable;
import me.lauriichan.spigot.justlootit.nms.capability.ICapability;
import me.lauriichan.spigot.justlootit.nms.capability.ICapabilityProvider;

public class DefaultCapabilityProvider implements ICapabilityProvider {

    @Override
    public <C extends Capable<C>> boolean isSupported(Class<C> type) {
        return true;
    }

    @Override
    public <C extends Capable<C>> void provide(VersionHandler versionHandler, Class<C> type, C value, List<ICapability> capabilities) {
        if (PlayerAdapter.class.isAssignableFrom(type)) {
            providePlayer(versionHandler, (PlayerAdapter) value, capabilities);
        }
    }
    
    private void providePlayer(VersionHandler versionHandler, PlayerAdapter player, List<ICapability> capabilities) {
        capabilities.add(new ActorCapability(player, versionHandler.serviceProvider()));
    }

}
