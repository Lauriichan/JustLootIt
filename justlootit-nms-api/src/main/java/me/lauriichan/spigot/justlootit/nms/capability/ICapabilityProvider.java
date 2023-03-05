package me.lauriichan.spigot.justlootit.nms.capability;

import java.util.List;

import me.lauriichan.spigot.justlootit.nms.VersionHandler;

public interface ICapabilityProvider {

    <C extends Capable<C>> boolean isSupported(Class<C> type);

    <C extends Capable<C>> void provide(VersionHandler versionHandler, Class<C> type, C value, List<ICapability> capabilities);

}
