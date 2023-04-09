package me.lauriichan.spigot.justlootit.capability;

import java.util.List;

import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.capability.Capable;
import me.lauriichan.spigot.justlootit.nms.capability.ICapability;
import me.lauriichan.spigot.justlootit.nms.capability.ICapabilityProvider;

public final class JustLootItCapabilityProvider implements ICapabilityProvider {

    public static final JustLootItCapabilityProvider CAPABILITY_PROVIDER = new JustLootItCapabilityProvider();

    private JustLootItCapabilityProvider() {}

    @Override
    public <C extends Capable<C>> boolean isSupported(Class<C> type) {
        return true;
    }

    @Override
    public <C extends Capable<C>> void provide(VersionHandler handler, Class<C> type, C value, List<ICapability> capabilities) {
        if (LevelAdapter.class.isAssignableFrom(type)) {
            provideLevel(handler, (LevelAdapter) value, capabilities);
        }
        if (PlayerAdapter.class.isAssignableFrom(type)) {
            providePlayer(handler, (PlayerAdapter) value, capabilities);
        }
    }

    private void provideLevel(VersionHandler handler, LevelAdapter adapter, List<ICapability> capabilities) {
        capabilities.add(new StorageCapability.LevelImpl(handler, adapter));
    }

    private void providePlayer(VersionHandler handler, PlayerAdapter adapter, List<ICapability> capabilities) {
        capabilities.add(new StorageCapability.PlayerImpl(handler, adapter));
    }

}
