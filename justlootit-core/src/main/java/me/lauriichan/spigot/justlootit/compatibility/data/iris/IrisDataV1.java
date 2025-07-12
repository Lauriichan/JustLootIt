package me.lauriichan.spigot.justlootit.compatibility.data.iris;

import org.bukkit.World;

import me.lauriichan.spigot.justlootit.compatibility.data.CompatibilityDataExtension;
import me.lauriichan.spigot.justlootit.compatibility.provider.CompatDependency;
import me.lauriichan.spigot.justlootit.compatibility.provider.iris.IIrisAccess;
import me.lauriichan.spigot.justlootit.compatibility.provider.iris.IIrisProvider;

public final class IrisDataV1 implements IIrisData {

    private final CompatibilityDataExtension<?> extension;
    private final IIrisTableKey[] keys;
    private final long seed;

    private volatile IIrisLootCache cache;

    public IrisDataV1(final CompatibilityDataExtension<?> extension, final IIrisTableKey[] keys, final long seed) {
        this.extension = extension;
        this.keys = keys;
        this.seed = seed;
    }

    @Override
    public CompatibilityDataExtension<?> extension() {
        return extension;
    }

    @Override
    public int version() {
        return 0;
    }
    
    @Override
    public long seed() {
        return seed;
    }

    @Override
    public IIrisTableKey[] keys() {
        return keys;
    }

    @Override
    public IIrisLootCache cache(IIrisAccess access, World world) {
        if (cache != null) {
            return cache;
        }
        IIrisProvider provider = CompatDependency.getActiveProvider(extension().id(), IIrisProvider.class);
        if (provider == null) {
            return null;
        }
        return cache = provider.access().loadLootTables(world, keys);
    }

}
