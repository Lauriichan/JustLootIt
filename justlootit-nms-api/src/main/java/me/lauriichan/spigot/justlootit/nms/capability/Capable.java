package me.lauriichan.spigot.justlootit.nms.capability;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;

public abstract class Capable<C extends Capable<C>> {

    private final Object2ObjectOpenHashMap<Class<? extends ICapability>, ICapability> capabilities = new Object2ObjectOpenHashMap<>(5);
    private final C self;
    private final Class<C> selfType;

    @SuppressWarnings("unchecked")
    public Capable() {
        try {
            this.self = (C) this;
            this.selfType = (Class<C>) self.getClass();
        } catch (final ClassCastException exp) {
            throw new IllegalStateException("C of Capable<C> has to be the class that extends Capable<C>", exp);
        }
    }

    public final <T extends ICapability> Optional<T> getCapability(final Class<T> type) {
        final ICapability capability = capabilities.get(type);
        if (capability != null) {
            return Optional.of(type.cast(capability));
        }
        for (final Entry<Class<? extends ICapability>, ICapability> current : capabilities.entrySet()) {
            if (type.isAssignableFrom(current.getKey())) {
                return Optional.of(type.cast(current.getValue()));
            }
        }
        return Optional.empty();
    }

    public final boolean hasCapability(final Class<? extends ICapability> type) {
        if (capabilities.containsKey(type)) {
            return true;
        }
        for (final Class<? extends ICapability> current : capabilities.keySet()) {
            if (type.isAssignableFrom(current)) {
                return true;
            }
        }
        return false;
    }

    public final Collection<ICapability> getCapabilities() {
        return capabilities.values();
    }

    public final void addCapabilities(final VersionHandler versionHandler, final ICapabilityProvider provider) {
        if (!provider.isSupported(selfType)) {
            return;
        }
        final ObjectArrayList<ICapability> capabilityList = new ObjectArrayList<>(5);
        provider.provide(versionHandler, selfType, self, capabilityList);
        if (capabilityList.isEmpty()) {
            return;
        }
        for (int index = 0; index < capabilityList.size(); index++) {
            final ICapability capability = capabilityList.get(index);
            final Class<? extends ICapability> type = capability.getClass();
            if (capabilities.containsKey(type)) {
                continue;
            }
            capabilities.put(type, capability);
        }
    }

}
