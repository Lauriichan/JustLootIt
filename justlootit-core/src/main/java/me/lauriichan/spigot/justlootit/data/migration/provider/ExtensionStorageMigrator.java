package me.lauriichan.spigot.justlootit.data.migration.provider;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.storage.StorageMigration;
import me.lauriichan.spigot.justlootit.storage.StorageMigrator;

public class ExtensionStorageMigrator extends StorageMigrator {

    public ExtensionStorageMigrator(JustLootItPlugin plugin) {
        super(plugin.logger());
        Object2ObjectArrayMap<Class<?>, ObjectArrayList<StorageMigration<?>>> tmpMigrations = new Object2ObjectArrayMap<>();
        plugin.extension(StorageMigrationExtension.class, true).callInstances(extension -> {
            Class<?> target = extension.targetType();
            if (target == null) {
                plugin.logger().warning("Couldn't register migration as it doesn't define a target: {0}", extension.getClass().getName());
                return;
            }
            if (extension.minVersion() < -1 || extension.minVersion() >= extension.targetVersion()) {
                plugin.logger().warning(
                    "Couldn't register migration as the min and/or target version are invalid: {0} (min: {1}, target: {2})",
                    extension.getClass().getName(), extension.minVersion(), extension.targetVersion());
                return;
            }
            ObjectArrayList<StorageMigration<?>> migrationList = tmpMigrations.get(target);
            if (migrationList == null) {
                migrationList = new ObjectArrayList<>();
                tmpMigrations.put(target, migrationList);
            }
            migrationList.add(extension);
        });
        if (tmpMigrations.isEmpty()) {
            return;
        }
        tmpMigrations.keySet().forEach(key -> {
            ObjectList<StorageMigration<?>> extensions = tmpMigrations.get(key);
            extensions.sort((m1, m2) -> {
                int tmp = Integer.compare(m1.minVersion(), m2.minVersion());
                if (tmp != 0) {
                    return tmp;
                }
                return Integer.compare(m1.targetVersion(), m2.targetVersion());
            });
            migrations.put(key, new Migration(extensions.get(extensions.size() - 1).targetVersion(), extensions));
        });
    }

}
