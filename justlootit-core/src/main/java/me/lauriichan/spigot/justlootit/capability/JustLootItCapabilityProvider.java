package me.lauriichan.spigot.justlootit.capability;

import java.util.List;

import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.capability.Capable;
import me.lauriichan.spigot.justlootit.nms.capability.ICapability;
import me.lauriichan.spigot.justlootit.nms.capability.ICapabilityProvider;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoWorld;

public final class JustLootItCapabilityProvider implements ICapabilityProvider {

    public static final JustLootItCapabilityProvider CAPABILITY_PROVIDER = new JustLootItCapabilityProvider();

    private JustLootItCapabilityProvider() {}

    @Override
    public <C extends Capable<C>> boolean isSupported(final Class<C> type) {
        return true;
    }

    @Override
    public <C extends Capable<C>> void provide(final VersionHandler handler, final Class<C> type, final C value,
        final List<ICapability> capabilities) {
        if (LevelAdapter.class.isAssignableFrom(type)) {
            provideLevel(handler, (LevelAdapter) value, capabilities);
        }
        if (ProtoWorld.class.isAssignableFrom(type)) {
            provideProtoWorld(handler, (ProtoWorld) value, capabilities);
        }
        if (PlayerAdapter.class.isAssignableFrom(type)) {
            providePlayer(handler, (PlayerAdapter) value, capabilities);
        }
    }

    private void provideLevel(final VersionHandler handler, final LevelAdapter adapter, final List<ICapability> capabilities) {
        capabilities.add(new StorageCapability.LevelContainerImpl(handler, adapter));
    }

    private void provideProtoWorld(final VersionHandler handler, final ProtoWorld world, final List<ICapability> capabilities) {
        capabilities.add(new StorageCapability.LevelContainerImpl(handler, world));
    }

    private void providePlayer(final VersionHandler handler, final PlayerAdapter adapter, final List<ICapability> capabilities) {
        capabilities.add(new StorageCapability.PlayerImpl(handler, adapter));
        capabilities.add(new PlayerGUICapability());
    }

}
