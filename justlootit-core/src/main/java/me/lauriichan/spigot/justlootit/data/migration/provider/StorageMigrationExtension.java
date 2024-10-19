package me.lauriichan.spigot.justlootit.data.migration.provider;

import me.lauriichan.minecraft.pluginbase.extension.ExtensionPoint;
import me.lauriichan.minecraft.pluginbase.extension.IExtension;
import me.lauriichan.spigot.justlootit.storage.StorageMigration;

@ExtensionPoint
public abstract class StorageMigrationExtension<T> extends StorageMigration<T> implements IExtension {

    public StorageMigrationExtension(Class<T> targetType, int minVersion, int targetVersion) {
        super(targetType, minVersion, targetVersion);
    }

}
