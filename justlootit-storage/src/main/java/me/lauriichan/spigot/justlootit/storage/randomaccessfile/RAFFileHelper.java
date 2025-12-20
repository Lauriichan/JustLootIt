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
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.versionized.RAFFile;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.versionized.RAFSettings;

public final class RAFFileHelper {

    private static final Predicate<String> IS_HEX = Pattern.compile("[a-fA-F0-9]+").asMatchPredicate();

    public static final FilenameFilter FILE_FILTER = (dir, name) -> {
        if (name.endsWith(RAFFileLegacy.FILE_EXTENSION)) {
            return IS_HEX.test(name.substring(0, name.length() - 4));
        } else if (name.endsWith(RAFFile.FILE_EXTENSION)) {
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
        } else if (name.endsWith(RAFFile.FILE_EXTENSION)) {
            return name.substring(0, name.length() - 5);
        }
        return name;
    }

    public static IRAFFile create(StorageAdapterRegistry registry, File file, RAFSettings settings) throws StorageException {
        RAFFile raf = new RAFFile(settings, file);
        upgradeFile(registry, raf, file, settings);
        return raf;
    }

    private static void upgradeFile(StorageAdapterRegistry registry, RAFFile raf, File file, RAFSettings settings)
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
                        raf.write(raf.newEntry(entry.id(), entry.typeId(), version, buffer));
                    }, Runnable::run);
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

    public static IRAFFile create(StorageAdapterRegistry registry, File file, int id, RAFSettings settings) throws StorageException {
        RAFFile raf = new RAFFile(settings, file, id);
        upgradeFile(registry, raf, file, id, settings);
        return raf;
    }

    private static void upgradeFile(StorageAdapterRegistry registry, RAFFile raf, File file, int id, RAFSettings settings)
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
                        raf.write(raf.newEntry(entry.id(), entry.typeId(), version, buffer));
                    }, Runnable::run);
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
