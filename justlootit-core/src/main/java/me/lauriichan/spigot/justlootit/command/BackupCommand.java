package me.lauriichan.spigot.justlootit.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.bukkit.entity.Player;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.annotation.Action;
import me.lauriichan.laylib.command.annotation.Argument;
import me.lauriichan.laylib.command.annotation.Command;
import me.lauriichan.laylib.command.annotation.Description;
import me.lauriichan.laylib.command.annotation.Param;
import me.lauriichan.laylib.command.annotation.Permission;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.resource.source.FileDataSource;
import me.lauriichan.minecraft.pluginbase.resource.source.IDataSource;
import me.lauriichan.spigot.justlootit.JustLootItPermission;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.command.argument.BackupArgument;
import me.lauriichan.spigot.justlootit.command.impl.LootItActor;
import me.lauriichan.spigot.justlootit.input.SimpleChatInputProvider;
import me.lauriichan.spigot.justlootit.message.Messages;
import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.storage.CachedStorage;
import me.lauriichan.spigot.justlootit.storage.IStorage;
import me.lauriichan.spigot.justlootit.storage.randomaccessfile.RAFMultiStorage;
import me.lauriichan.spigot.justlootit.nms.util.IOUtil;

@Extension
@Command(name = "backup")
@Permission(JustLootItPermission.COMMAND_BACKUP)
public class BackupCommand implements ICommandExtension {

    public static final int BACKUP_PAGE_SIZE = 12;

    private static record BackupDir(File directory, UUID worldUID) {}

    private static record BackupApply(IDataSource source, String fileName) {}

    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private static final DecimalFormat DEC_FORMAT = new DecimalFormat("0.0");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("YYYY_MM_dd-HH_mm_ss.SSS_Z");

    private final SimpleChatInputProvider inputProvider = SimpleChatInputProvider.CHAT;

    @Action("create")
    @Description("$#command.description.justlootit.backup.create")
    public void create(final LootItActor<?> actor) {
        actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_CREATE_STEP_COLLECT);
        ObjectArrayList<BackupDir> directoriesToBackup = new ObjectArrayList<>();
        actor.versionHandler().forEachLevel(level -> {
            IStorage storage = level.getCapability(StorageCapability.class).get().storage();
            if (storage instanceof CachedStorage cached) {
                storage = cached.delegate();
            }
            directoriesToBackup.add(new BackupDir(((RAFMultiStorage) storage).directory(), level.asBukkit().getUID()));
        });
        File playerDir = new File(actor.versionHelper().globalDataFolder(), StorageCapability.PLAYER_PATH);
        String fileName = TIME_FORMATTER.format(OffsetDateTime.now()) + ".zip";
        File zipFile = new File(actor.plugin().resource("data://backup/data/" + fileName).getPath());
        actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_CREATE_STEP_PACK_START);
        if (zipFile.getParentFile() != null && !zipFile.getParentFile().exists()) {
            zipFile.getParentFile().mkdirs();
        }
        try {
            zipFile.createNewFile();
            try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile), StandardCharsets.UTF_8)) {
                writeToZip(zipOut, "player_data", playerDir);
                for (BackupDir dir : directoriesToBackup) {
                    writeToZip(zipOut, dir.worldUID().toString(), dir.directory());
                }
            }
        } catch (IOException e) {
            actor.logger().error("Failed to pack JLI data files", e);
            actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_CREATE_STEP_PACK_FAILED);
            return;
        }
        actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_CREATE_DONE, Key.of("name", fileName));
    }

    private void writeToZip(ZipOutputStream zipOut, String dirName, File directory) throws IOException {
        if (!dirName.endsWith("/")) {
            dirName += '/';
        }
        ZipEntry entry = new ZipEntry(dirName);
        zipOut.putNextEntry(entry);
        zipOut.closeEntry();
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                // Ignore any sub-directories for now
                continue;
            }
            entry = new ZipEntry(dirName + file.getName());
            entry.setSize(file.length());
            zipOut.putNextEntry(entry);
            try (FileInputStream fileIn = new FileInputStream(file)) {
                fileIn.transferTo(zipOut);
            }
            zipOut.closeEntry();
        }
    }

    @Action("list")
    @Description("$#command.description.justlootit.backup.list")
    public void list(final LootItActor<?> actor, @Argument(name = "page", optional = true, index = 1, params = {
        @Param(name = "minimum", type = Param.TYPE_INT, intValue = 1)
    }) int page) {
        Path backupPath = new File(actor.plugin().resource("data://backup/data").getPath()).toPath();
        ObjectList<String> backups = BackupArgument.listBackups(backupPath);
        if (backups.isEmpty()) {
            actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_LIST_NO_ENTRIES);
            return;
        }
        int maxPage = Math.floorDiv(backups.size(), BACKUP_PAGE_SIZE) + (backups.size() % BACKUP_PAGE_SIZE != 0 ? 1 : 0);
        page = Math.min(Math.max(page, 1), maxPage);
        actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_LIST_HEADER_FORMAT_START, Key.of("page", page), Key.of("maxPage", maxPage));
        int maxIndex = Math.min((page - 1) * BACKUP_PAGE_SIZE + BACKUP_PAGE_SIZE, backups.size());
        for (int index = (page - 1) * BACKUP_PAGE_SIZE; index < maxIndex; index++) {
            String backupName = backups.get(index);
            long fileSize = -1L;
            Path backupFilePath = backupPath.resolve(backupName + ".zip");
            try {
                if (Files.exists(backupFilePath)) {
                    fileSize = Files.size(backupFilePath);
                } else if (Files.exists(backupFilePath = backupPath.resolve(backupName))) {
                    fileSize = Files.size(backupFilePath);
                }
            } catch (IOException _ignore) {
            }
            actor.actionMessageBuilder()
                .message(Messages.COMMAND_BACKUP_LIST_ENTRY_TEXT, Key.of("name", backups.get(index)),
                    Key.of("fileSize", formatFileSize(fileSize)))
                .actionHover(Messages.COMMAND_BACKUP_LIST_ENTRY_HOVER)
                .action(me.lauriichan.laylib.command.Action.suggest("/jli backup apply %s".formatted(backups.get(index)))).send(actor);
        }
        actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_LIST_HEADER_FORMAT_END, Key.of("page", page), Key.of("maxPage", maxPage));
    }

    private String formatFileSize(long fileSize) {
        if (fileSize == -1) {
            return "? B";
        }
        if (fileSize < 1024) {
            return fileSize + " B";
        }
        double tmp;
        if ((tmp = fileSize / 1024d) < 1024) {
            return DEC_FORMAT.format(tmp) + " KiB";
        }
        if ((tmp /= 1024) < 1024) {
            return DEC_FORMAT.format(tmp) + " MiB";
        }
        return DEC_FORMAT.format(tmp / 1024) + " GiB";
    }

    @Action("apply")
    @Description("$#command.description.justlootit.backup.apply")
    public void apply(final LootItActor<?> actor,
        @Argument(name = "backup name", optional = true, index = 1) BackupArgument.Backup backup) {
        String fileName;
        IDataSource source;
        if (backup != null) {
            fileName = backup.name();
            if (fileName.endsWith(".zip")) {
                fileName = fileName.substring(0, fileName.length() - 4);
            }
            int slashIdx = fileName.lastIndexOf('/');
            if (slashIdx != -1) {
                fileName = fileName.substring(slashIdx);
            }
            source = actor.plugin().resource("data://backup/data/" + fileName + ".zip");
            if (!source.exists()) {
                actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_INIT_UNKNOWN_BACKUP, Key.of("name", fileName));
                return;
            }
        } else {
            File directory = new File(actor.plugin().resource("data://backup/data").getPath());
            File backupFile = null;
            OffsetDateTime newest = null, current;
            for (File file : directory.listFiles()) {
                if (!file.getName().endsWith(".zip") || !file.isFile()) {
                    continue;
                }
                try {
                    current = OffsetDateTime.parse(file.getName().substring(0, 4), TIME_FORMATTER);
                } catch (DateTimeParseException _ignore) {
                    long lastModified = file.lastModified();
                    if (lastModified <= 0) {
                        continue;
                    }
                    current = OffsetDateTime.ofInstant(Instant.ofEpochMilli(lastModified), UTC_ZONE);
                }
                if (newest == null || current.isAfter(newest)) {
                    newest = current;
                    backupFile = file;
                }
            }
            if (backupFile == null) {
                actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_INIT_NO_BACKUP);
                return;
            }
            fileName = backupFile.getName();
            source = new FileDataSource(backupFile);
            actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_INIT_LATEST, Key.of("name", fileName));
        }

        actor.attributes().attrSet("BackupApply", new BackupApply(source, fileName));
        inputProvider.getBooleanInput(actor,
            actor.getTranslatedMessageAsString(Messages.INPUT_PROMPT_BACKUP_APPLY_ARE_YOU_SURE, Key.of("backupName", fileName)),
            actor.getTranslatedMessageAsString(Messages.INPUT_RETRY_BOOLEAN),
            (act, state) -> actor.plugin().scheduler().async(() -> doApplyBackup(act, state)));
    }

    private void doApplyBackup(Actor<?> uncastedActor, Boolean state) {
        if (state == null || !state) {
            uncastedActor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_INIT_ABORTED);
            return;
        }
        LootItActor<?> actor = (LootItActor<?>) uncastedActor;
        BackupApply apply = actor.attributes().attr("BackupApply", BackupApply.class);
        IDataSource source = apply.source();
        String fileName = apply.fileName();
        Path tempDir = new File(actor.plugin().resource("data://backup/tmp/" + fileName).getPath()).toPath();
        Key backupKey = Key.of("name", fileName);
        try {
            if (Files.exists(tempDir)) {
                actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_STEP_CLEAN_TEMPORARY_START, backupKey);
                IOUtil.delete(tempDir);
                actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_STEP_CLEAN_TEMPORARY_DONE, backupKey);
            }
        } catch (IOException e) {
            actor.logger().error("Failed to clean temporary files of backup '{0}'", e, fileName);
            actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_STEP_CLEAN_TEMPORARY_FAILED, backupKey);
            return;
        }
        try {
            try {
                actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_STEP_UNPACK_START, backupKey);
                Files.createDirectories(tempDir);
                try (ZipInputStream zipIn = new ZipInputStream(source.openReadableStream(), StandardCharsets.UTF_8)) {
                    ZipEntry entry;
                    while ((entry = zipIn.getNextEntry()) != null) {
                        if (entry.isDirectory()) {
                            Files.createDirectories(tempDir.resolve(entry.getName()));
                            continue;
                        }
                        Path outPath = tempDir.resolve(entry.getName());
                        Files.createDirectories(outPath.getParent());
                        try (OutputStream outputStream = outPath.getFileSystem().provider().newOutputStream(outPath,
                            StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING)) {
                            zipIn.transferTo(outputStream);
                        }
                    }
                }
                actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_STEP_UNPACK_DONE, backupKey);
            } catch (IOException e) {
                actor.logger().error("Failed to unpack files of backup '{0}'", e, fileName);
                actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_STEP_UNPACK_FAILED, backupKey);
                return;
            }
            VersionHandler handler = actor.versionHandler();
            Path backedPlayerDir = tempDir.resolve("player_data");
            if (Files.exists(backedPlayerDir)) {
                Path playerDir = new File(actor.versionHelper().globalDataFolder(), StorageCapability.PLAYER_PATH).toPath();
                actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_STEP_CLEAN_PERMANENT_PLAYER_PREPARE);
                handler.forEachPlayer(player -> {
                    Player bktPlayer = player.asBukkit();
                    handler.platform().scheduler().syncEntity(bktPlayer, bktPlayer::closeInventory).join();
                    player.terminate();
                });
                try {
                    try {
                        if (Files.exists(playerDir)) {
                            actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_STEP_CLEAN_PERMANENT_PLAYER_START, backupKey);
                            IOUtil.delete(playerDir);
                            actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_STEP_CLEAN_PERMANENT_PLAYER_DONE, backupKey);
                        }
                    } catch (IOException e) {
                        actor.logger().error("Failed to delete player data files", e);
                        actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_STEP_CLEAN_PERMANENT_PLAYER_FAILED, backupKey);
                        return;
                    }
                    try {
                        actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_STEP_APPLY_PLAYER_START, backupKey);
                        Files.createDirectories(playerDir);
                        IOUtil.move(backedPlayerDir, playerDir);
                        actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_STEP_APPLY_PLAYER_DONE, backupKey);
                    } catch (IOException e) {
                        actor.logger().error("Failed to apply player data files of backup '{0}'", e, fileName);
                        actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_STEP_APPLY_PLAYER_FAILED, backupKey);
                        return;
                    }
                } finally {
                    handler.forEachPlayer(player -> {
                        player.reset();
                        handler.applyCapabilities(player);
                    });
                }
            }
            Iterator<Path> levels;
            try {
                levels = IOUtil.list(tempDir);
            } catch (IOException e) {
                actor.logger().error("Failed to list files in temporary file directory of backup '{0}'", e, fileName);
                actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_STEP_APPLY_LIST_FAILED, backupKey);
                return;
            }
            while (levels.hasNext()) {
                Path levelDataPath = levels.next();
                UUID levelId;
                try {
                    levelId = UUID.fromString(levelDataPath.getFileName().toString());
                } catch (IllegalArgumentException _ignore) {
                    continue;
                }
                LevelAdapter level = handler.getLevel(levelId);
                if (level == null) {
                    actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_STEP_APPLY_FOUND_BUT_UNKNOWN, backupKey,
                        Key.of("id", levelId));
                    continue;
                }
                if (level.isTerminated()) {
                    // Skip silently
                    continue;
                }
                String levelName = level.asBukkit().getName();
                Key levelKey = Key.of("level", levelName);
                level.terminate();
                try {
                    Path storagePath = level.dataFolder().toPath().resolve(StorageCapability.CONTAINER_PATH);
                    try {
                        if (Files.exists(storagePath)) {
                            actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_STEP_CLEAN_PERMANENT_LEVEL_START, backupKey,
                                levelKey);
                            IOUtil.delete(storagePath);
                            actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_STEP_CLEAN_PERMANENT_LEVEL_DONE, backupKey, levelKey);
                        }
                    } catch (IOException e) {
                        actor.logger().error("Failed to delete container data files of level '{0}'", e, levelName);
                        actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_STEP_CLEAN_PERMANENT_LEVEL_FAILED, backupKey, levelKey);
                        continue;
                    }
                    try {
                        actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_STEP_APPLY_LEVEL_START, backupKey, levelKey);
                        IOUtil.move(levelDataPath, storagePath);
                    } catch (IOException e) {
                        actor.logger().error("Failed to apply container data files of backup '{1}' for level '{0}'", e, levelName,
                            fileName);
                        actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_STEP_APPLY_LEVEL_FAILED, backupKey, levelKey);
                        continue;
                    }
                    actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_STEP_APPLY_LEVEL_DONE, backupKey, levelKey);
                } finally {
                    level.reset();
                    handler.applyCapabilities(level);
                }
            }
            actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_DONE, backupKey);
        } finally {
            try {
                if (Files.exists(tempDir)) {
                    actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_STEP_CLEAN_TEMPORARY_START, backupKey);
                    IOUtil.delete(tempDir);
                    actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_STEP_CLEAN_TEMPORARY_DONE, backupKey);
                }
            } catch (IOException e) {
                actor.logger().error("Failed to clean temporary files of backup '{0}'", e, fileName);
                actor.sendTranslatedMessage(Messages.COMMAND_BACKUP_APPLY_STEP_CLEAN_TEMPORARY_FAILED, backupKey);
            }
        }
    }

}
