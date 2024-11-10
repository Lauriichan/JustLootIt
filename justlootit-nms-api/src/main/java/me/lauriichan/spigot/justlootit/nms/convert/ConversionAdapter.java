package me.lauriichan.spigot.justlootit.nms.convert;

import java.io.File;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.storage.util.executor.ProtoExecutor;
import me.lauriichan.spigot.justlootit.storage.util.executor.ProtoExecutor.ThreadSupplier;

public abstract class ConversionAdapter implements AutoCloseable {

    private volatile ProtoExecutor<ConvThread> executor;

    public final ProtoExecutor<ConvThread> executor() {
        return executor;
    }

    protected final ProtoExecutor<ConvThread> workerPool(ISimpleLogger logger) {
        if (executor != null) {
            return executor;
        }
        return executor = new ProtoExecutor<>(logger, new ThreadSupplier<>() {
            @Override
            public ConvThread[] createArray(int size) {
                return new ConvThread[size];
            }

            @Override
            public ConvThread createThread(int id, ProtoExecutor<ConvThread> executor) {
                return new ConvThread(id, executor);
            }
        }, (log, cpu, threads) -> {
            log.info("Conversion will run with {0} threads.", threads);
            if (threads > cpu) {
                log.warning("There are less cpus available ({0}) than allocated, this might slow down the conversion a little.", cpu);
            }
        });
    }

    public abstract ProtoWorld getWorld(File directory);

    public void close() {
        if (executor != null) {
            executor.setInactive();
            executor.await();
        }
    }

}
