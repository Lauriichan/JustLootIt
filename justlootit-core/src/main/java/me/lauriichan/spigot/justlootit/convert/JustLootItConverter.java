package me.lauriichan.spigot.justlootit.convert;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.bukkit.Bukkit;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.laylib.reflection.StackTracker;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.convert.ConversionAdapter;
import me.lauriichan.spigot.justlootit.nms.convert.ConversionProgress;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoChunk;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoWorld;

public final class JustLootItConverter {
    
    private static final DecimalFormat PROGRESS_FORMAT = new DecimalFormat("0.00%");

    private JustLootItConverter() {
        throw new UnsupportedOperationException();
    }

    private static void createConverters(ObjectArrayList<ChunkConverter> converters, VersionHandler versionHandler, ConversionProperties properties) {
        addConverter(converters, new LootinConverter(versionHandler, properties));
        addConverter(converters, new VanillaConverter(versionHandler, properties));
    }

    private static void addConverter(ObjectArrayList<ChunkConverter> converters, ChunkConverter converter) {
        if (!converter.isEnabled()) {
            return;
        }
        converters.add(converter);
    }

    public static boolean convert(VersionHandler versionHandler, ConversionProperties properties) {
        Class<?> clazz = StackTracker.getCallerClass().orElse(null);
        if (clazz != JustLootItPlugin.class) {
            throw new UnsupportedOperationException();
        }
        ConversionAdapter conversionAdapter = versionHandler.conversionAdapter();
        ObjectArrayList<ChunkConverter> converters = new ObjectArrayList<>();
        createConverters(converters, versionHandler, properties);
        if (converters.isEmpty()) {
            return false;
        }
        File worldContainer = Bukkit.getWorldContainer();
        File[] possibleWorldFiles = worldContainer.listFiles();
        boolean somethingWasConverted = false;
        ISimpleLogger logger = versionHandler.logger();
        ObjectList<String> blacklistedWorldNames = properties.getPropertyEntries(ConvProp.BLACKLISTED_WORLDS);
        for (File file : possibleWorldFiles) {
            if (!file.isDirectory() || blacklistedWorldNames.contains(file.getName())) {
                continue;
            }
            try (ProtoWorld world = conversionAdapter.getWorld(file)) {
                if (world == null) {
                    continue;
                }
                ChunkConverter[] enabledConverters = converters.stream().filter(converter -> converter.isEnabledFor(world)).toArray(ChunkConverter[]::new);
                if (enabledConverters.length == 0) {
                    continue;
                }
                Consumer<ProtoChunk> chunkConsumer = chunk -> {
                    Random random = new Random(chunk.getWorld().getSeed() | chunk.getPosAsLong());
                    for (ChunkConverter converter : enabledConverters) {
                        converter.convert(chunk, random);
                    }
                };
                somethingWasConverted = true;
                logger.info("Starting conversion of level '{0}'...", world.getName());
                ConversionProgress progress = world.streamChunks(chunkConsumer);
                if (!progress.hasNext()) {
                    logger.info("Skipping level '{0}', couldn't find any regions.", world.getName());
                    continue;
                }
                loop:
                while (true) {
                    logger.info("Converting level '{0}'... ({2}) [{1} / {3} Chunks]", world.getName(), progress.counter().current(), PROGRESS_FORMAT.format(progress.counter().progress()), progress.counter().max());
                    while (progress.future().isDone()) {
                        if (!progress.next()) {
                            break loop;
                        }
                    }
                    try {
                        progress.future().get(5, TimeUnit.SECONDS);
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        continue;
                    }
                } 
                logger.info("Conversion of level '{0}' done! [{1} Chunks]", world.getName(), progress.counter().max());
            }
        }
        return somethingWasConverted;
    }

}
