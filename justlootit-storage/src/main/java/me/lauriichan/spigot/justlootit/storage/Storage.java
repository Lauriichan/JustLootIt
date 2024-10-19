package me.lauriichan.spigot.justlootit.storage;

import me.lauriichan.laylib.logger.ISimpleLogger;

public abstract class Storage implements IStorage {

    protected final StorageAdapterRegistry registry;
    protected final ISimpleLogger logger;

    public Storage(final StorageAdapterRegistry registry) {
        this.registry = registry;
        this.logger = registry.migrator().logger();
    }

    @Override
    public final ISimpleLogger logger() {
        return logger;
    }

    @Override
    public final StorageAdapterRegistry registry() {
        return registry;
    }

}
