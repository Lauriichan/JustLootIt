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
import me.lauriichan.spigot.justlootit.nms.convert.ConvThread;
import me.lauriichan.spigot.justlootit.nms.convert.ConversionAdapter;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoChunk;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoWorld;
import me.lauriichan.spigot.justlootit.storage.util.counter.CounterProgress;

public final class JustLootItConverter {

    private static final DecimalFormat PROGRESS_FORMAT = new DecimalFormat("0.00%");
    
    private static final long THREAD_DUMP_TIMEOUT_INFO = TimeUnit.SECONDS.toMillis(15);

    private JustLootItConverter() {
        throw new UnsupportedOperationException();
    }

    private static void createConverters(ObjectArrayList<ChunkConverter> converters, JustLootItPlugin plugin, VersionHandler versionHandler,
        ConversionProperties properties) {
        addConverter(converters, new MigrationConverter(plugin, versionHandler, properties));
        addConverter(converters, new LootinConverter(versionHandler, properties));
        addConverter(converters, new VanillaConverter(versionHandler, properties));
    }

    private static void addConverter(ObjectArrayList<ChunkConverter> converters, ChunkConverter converter) {
        if (!converter.isEnabled()) {
            return;
        }
        converters.add(converter);
    }

    public static boolean convert(JustLootItPlugin plugin, VersionHandler versionHandler, ConversionProperties properties) {
        Class<?> clazz = StackTracker.getCallerClass().orElse(null);
        if (clazz != JustLootItPlugin.class) {
            throw new UnsupportedOperationException();
        }
        ObjectArrayList<ChunkConverter> converters = new ObjectArrayList<>();
        createConverters(converters, plugin, versionHandler, properties);
        if (converters.isEmpty()) {
            return false;
        }
        try (ConversionAdapter conversionAdapter = versionHandler.conversionAdapter()) {
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
                    ChunkConverter[] enabledConverters = converters.stream().filter(converter -> converter.isEnabledFor(world))
                        .toArray(ChunkConverter[]::new);
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
                    CounterProgress progress = world.streamChunks(chunkConsumer);
                    if (!progress.hasFutures()) {
                        logger.info("Skipping level '{0}', couldn't find any regions.", world.getName());
                        continue;
                    }
                    long now = System.currentTimeMillis(), dumpTime = now;
                    loop:
                    while (true) {
                        if (now > dumpTime + THREAD_DUMP_TIMEOUT_INFO) {
                            dumpTime = now;
                            printThreads(logger, world.getName(), progress, conversionAdapter.executor().threads());
                        } else {
                            logger.info("Converting level '{0}'... ({2}) [{1} / {3} Chunks]", world.getName(), progress.counter().current(),
                                PROGRESS_FORMAT.format(progress.counter().progress()), progress.counter().max());
                        }
                        while (progress.future().isDone()) {
                            if (!progress.next()) {
                                break loop;
                            }
                            dumpTime = now;
                        }
                        try {
                            progress.future().get(5, TimeUnit.SECONDS);
                        } catch (InterruptedException | ExecutionException | TimeoutException e) {
                            continue;
                        } finally {
                            now = System.currentTimeMillis();
                        }
                    }
                    logger.info("Conversion of level '{0}' done! [{1} Chunks]", world.getName(), progress.counter().max());
                } catch (RuntimeException exp) {
                    logger.error("Couldn't start conversion of world '{0}'...", file.getName(), exp);
                }
            }
            return somethingWasConverted;
        }
    }

    private static void printThreads(ISimpleLogger logger, String level, CounterProgress progress, ObjectList<ConvThread> threads) {
        logger.info("═════════════════════════════════════════════════════");
        logger.info(" ");
        logger.info("  ┌──► Conversion ({0})", level);
        logger.info("  │");
        logger.info("  │ 15 seconds passed without future update,");
        logger.info("  │ printing progress and thread information.");
        logger.info("  │");
        logger.info("  ├► THREADS:      {0}", threads.size());
        logger.info("  │");
        logger.info("  ├► FUTURE TOTAL: {0}", progress.futureCount());
        logger.info("  ├► FUTURE IDX:   {0}", progress.futureIndex());
        logger.info("  │");
        logger.info("  ├► CHUNKS TOTAL: {0}", progress.counter().max());
        logger.info("  ├► CHUNKS DONE:  {0}", progress.counter().current());
        logger.info("  │");
        logger.info("  ├► PROGRESS:     {0}", PROGRESS_FORMAT.format(progress.counter().progress()));
        logger.info("  │");
        logger.info("  └──► Conversion ({0})", level);
        logger.info(" ");
        logger.info(" ");
        logger.info(" ");
        for (ConvThread thread : threads) {
            logger.info("  ┌──► {0}", thread.getName());
            logger.info("  │");
            if (thread.region() == null) {
                logger.info("  ├► STATE:  IDLE");
                logger.info("  │");
                logger.info("  └──► {0}", thread.getName());
                logger.info(" ");
                continue;
            }
            logger.info("  ├► STATE:  WORKING");
            logger.info("  │");
            logger.info("  ├► REGION: {0}", nonNull(thread.region()));
            logger.info("  ├► CHUNK:  {0}, {1}", thread.cx(), thread.cz());
            logger.info("  ├► TASK:   {0}", nonNull(thread.task()));
            logger.info("  │");
            logger.info("  ├─► Stacktrace");
            logger.info("  │");
            StringBuilder stack = new StringBuilder("  │ ");
            StackTraceElement[] elements = thread.getStackTrace();
            for (int i = elements.length - 1; i >= 0; i--) {
                StackTraceElement element = elements[i];
                String fileName = element.getFileName();
                stack.append("at ").append(reformatClassName(element.getClassName())).append('.').append(element.getMethodName())
                    .append('(');
                if (fileName == null) {
                    stack.append("Unknown Source");
                } else {
                    stack.append(fileName).append(':').append(Integer.toString(element.getLineNumber()));
                }
                logger.info(stack.append(')').toString());
                stack = new StringBuilder("  │ ");
            }
            logger.info("  │");
            logger.info("  └──► {0}", thread.getName());
            logger.info(" ");
        }
        logger.info("═════════════════════════════════════════════════════");
    }
    
    private static String reformatClassName(String className) {
        String[] parts = className.split("\\.");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            char[] buf = new char[2];
            parts[i].getChars(0, 2, buf, 0);
            if (buf[0] == 'v' && Character.isDigit(buf[1])) {
                builder.append(parts[i]).append('.');
                continue;
            }
            builder.append(buf).append('.');
        }
        return builder.append(parts[parts.length - 1]).toString();
    }
    
    private static String nonNull(String string) {
        if (string == null) {
            return "";
        }
        return string;
    }

}
