package me.lauriichan.spigot.justlootit.storage;

import io.netty.buffer.ByteBuf;

public abstract class StorageMigration<T> {

    private final Class<T> targetType;
    private final int minVersion, targetVersion;
    
    public StorageMigration(Class<T> targetType, int minVersion, int targetVersion) {
        this.targetType = targetType;
        this.minVersion = minVersion;
        this.targetVersion = targetVersion;
    }
    
    public final Class<T> targetType() {
        return targetType;
    }
    
    public final int minVersion() {
        return minVersion;
    }
    
    public final int targetVersion() {
        return targetVersion;
    }
    
    public abstract String description();
    
    public abstract void migrate(StorageMigrator migrator, ByteBuf inBuffer, ByteBuf outBuffer) throws Throwable;
    
}
