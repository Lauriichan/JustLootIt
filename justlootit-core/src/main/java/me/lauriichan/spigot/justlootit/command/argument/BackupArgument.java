package me.lauriichan.spigot.justlootit.command.argument;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.IArgumentMap;
import me.lauriichan.laylib.command.IArgumentType;
import me.lauriichan.laylib.command.Suggestions;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.nms.util.IOUtil;
import me.lauriichan.spigot.justlootit.util.ImprovedLevenshteinDistance;

public final class BackupArgument implements IArgumentType<BackupArgument.Backup> {

    public static record Backup(String name) {}

    private final Path path;

    public BackupArgument() {
        this.path = new File(JustLootItPlugin.get().resource("data://backup/data").getPath()).toPath();
    }

    @Override
    public Backup parse(final Actor<?> actor, String input, final IArgumentMap map) throws IllegalArgumentException {
        return new Backup(input);
    }

    @Override
    public void suggest(final Actor<?> actor, final String input, final Suggestions suggestions, final IArgumentMap map) {
        for (final Map.Entry<String, Integer> entry : ImprovedLevenshteinDistance.rankByDistance(input, listBackups(path))) {
            suggestions.suggest(entry.getValue(), entry.getKey());
        }
    }

    public static ObjectList<String> listBackups(Path dirPath) {
        Iterator<Path> iter;
        try {
            iter = IOUtil.list(dirPath);
        } catch (IOException _ignore) {
            return ObjectLists.emptyList();
        }
        ObjectArrayList<String> backups = new ObjectArrayList<>();
        while (iter.hasNext()) {
            Path path = iter.next();
            if (!Files.isRegularFile(path)) {
                continue;
            }
            String fileName = path.getFileName().toString();
            if (fileName.endsWith(".zip")) {
                fileName = fileName.substring(0, fileName.length() - 4);
            }
            backups.add(fileName);
        }
        return ObjectLists.unmodifiable(backups);
    }

}
