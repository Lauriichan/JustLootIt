package me.lauriichan.spigot.justlootit.storage.randomaccessfile;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import io.netty.buffer.ByteBuf;
import me.lauriichan.spigot.justlootit.storage.StorageAdapterRegistry;
import me.lauriichan.spigot.justlootit.storage.StorageException;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.legacy.RAFFileLegacy;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.legacy.RAFSettingsLegacy;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.v0.RAFFileV0;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.v0.RAFSettingsV0;

public final class RAFFileHelper {

    private static final Predicate<String> IS_HEX = Pattern.compile("[a-fA-F0-9]+").asMatchPredicate();

    public static final FilenameFilter FILE_FILTER = (dir, name) -> {
        if (name.endsWith(RAFFileLegacy.FILE_EXTENSION)) {
            return IS_HEX.test(name.substring(0, name.length() - 4));
        } else if (name.endsWith(RAFFileV0.FILE_EXTENSION)) {
            return IS_HEX.test(name.substring(0, name.length() - 5));
        }
        return false;
    };

    private RAFFileHelper() {
        throw new UnsupportedOperationException();
    }

    public static String getRAFFileName(File file) {
        String name = file.getName();
        if (name.endsWith(RAFFileLegacy.FILE_EXTENSION)) {
            return name.substring(0, name.length() - 4);
        } else if (name.endsWith(RAFFileV0.FILE_EXTENSION)) {
            return name.substring(0, name.length() - 5);
        }
        return name;
    }

    public static IRAFEntry newEntry(long id, int typeId, int version, ByteBuf buffer) {
        return new RAFFileV0.RAFEntry(id, typeId, version, buffer);
    }

    public static IRAFFile create(StorageAdapterRegistry registry, File file) throws StorageException {
        return create(registry, file, RAFSettingsV0.DEFAULT);
    }

    public static IRAFFile create(StorageAdapterRegistry registry, File file, int id) throws StorageException {
        return create(registry, file, id, RAFSettingsV0.DEFAULT);
    }

    public static IRAFFile create(StorageAdapterRegistry registry, File file, RAFSettingsV0 settings) throws StorageException {
        RAFFileV0 raf = new RAFFileV0(settings, file);
        upgradeFile(registry, raf, file, settings);
        return raf;
    }

    private static void upgradeFile(StorageAdapterRegistry registry, RAFFileV0 raf, File file, RAFSettingsV0 settings)
        throws StorageException {
        try {
            if (RAFFileLegacy.create(file).exists()) {
                RAFFileLegacy legacy = new RAFFileLegacy(RAFSettingsLegacy.of(settings), file);
                legacy.open();
                raf.open();
                try {
                    registry.migrator().logger().info("Upgrading file '{0}'...", legacy.file().getPath());
                    legacy.forEach(entry -> {
                        Class<?> type = registry.findAdapter(entry.typeId()).type();
                        int version = entry.version() == -1 ? 0 : entry.version();
                        ByteBuf buffer = entry.buffer();
                        if (registry.migrator().needsMigration(type, entry.version())) {
                            Map.Entry<Integer, ByteBuf> newEntry = registry.migrator().migrate(entry.id(), type, entry.version(), entry.buffer());
                            if (newEntry.getValue() != entry.buffer()) {
                                version = newEntry.getKey().intValue();
                                buffer = newEntry.getValue();
                            }
                        }
                        raf.write(newEntry(entry.id(), entry.typeId(), version, buffer));
                    });
                    registry.migrator().logger().info("File '{0}' successfully upgraded to '{1}'.", legacy.file().getPath(),
                        raf.file().getPath());
                } finally {
                    raf.close();
                    legacy.close();
                }
                // This does not execute if an error occurs
                legacy.file().delete();
            }
        } catch (RuntimeException exp) {
            throw new StorageException("Failed to upgrade legacy file '" + file.getName() + "'", exp);
        }
    }

    public static IRAFFile create(StorageAdapterRegistry registry, File file, int id, RAFSettingsV0 settings) throws StorageException {
        RAFFileV0 raf = new RAFFileV0(settings, file, id);
        upgradeFile(registry, raf, file, id, settings);
        return raf;
    }

    private static void upgradeFile(StorageAdapterRegistry registry, RAFFileV0 raf, File file, int id, RAFSettingsV0 settings)
        throws StorageException {
        try {
            if (RAFFileLegacy.create(file, id).exists()) {
                RAFFileLegacy legacy = new RAFFileLegacy(RAFSettingsLegacy.of(settings), file, id);
                legacy.open();
                raf.open();
                try {
                    registry.migrator().logger().info("Upgrading file '{0}'...", legacy.file().getPath());
                    legacy.forEach(entry -> {
                        Class<?> type = registry.findAdapter(entry.typeId()).type();
                        int version = entry.version() == -1 ? 0 : entry.version();
                        ByteBuf buffer = entry.buffer();
                        if (registry.migrator().needsMigration(type, entry.version())) {
                            Map.Entry<Integer, ByteBuf> newEntry = registry.migrator().migrate(entry.id(), type, entry.version(), entry.buffer());
                            if (newEntry.getValue() != entry.buffer()) {
                                version = newEntry.getKey().intValue();
                                buffer = newEntry.getValue();
                            }
                        }
                        raf.write(newEntry(entry.id(), entry.typeId(), version, buffer));
                    });
                    registry.migrator().logger().info("File '{0}' successfully upgraded to '{1}'.", legacy.file().getPath(),
                        raf.file().getPath());
                } finally {
                    raf.close();
                    legacy.close();
                }
                // This does not execute if an error occurs
                legacy.file().delete();
            }
        } catch (RuntimeException exp) {
            throw new StorageException("Failed to upgrade legacy file '" + file.getName() + "'", exp);
        }
    }

}
