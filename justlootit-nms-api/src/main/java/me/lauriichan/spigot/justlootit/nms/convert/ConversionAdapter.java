package me.lauriichan.spigot.justlootit.nms.convert;

import java.io.File;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.storage.util.executor.ProtoExecutor;

public abstract class ConversionAdapter implements AutoCloseable {

    private volatile ProtoExecutor<ConvThread> executor;

    public final ProtoExecutor<ConvThread> executor() {
        return executor;
    }

    protected final ProtoExecutor<ConvThread> workerPool(ISimpleLogger logger) {
        if (executor != null) {
            return executor;
        }
        return executor = ProtoExecutor.<ConvThread>of(new ProtoExecutor.ThreadSupplier<ConvThread>() {
            @Override
            public ConvThread[] createArray(int size) {
                return new ConvThread[size];
            }

            @Override
            public ConvThread createThread(int id, ProtoExecutor<ConvThread> executor) {
                return new ConvThread(id, executor);
            }
        }).info((log, cpu, threads) -> {
            log.info("Conversion will run with {0} threads.", threads);
            if (threads > cpu) {
                log.warning("There are less cpus available ({0}) than allocated, this might slow down the conversion a little.", cpu);
            }
        }).logger(logger).minThreads(4).maxThreads(32).percentage(0.75f).build();
    }

    public abstract ProtoWorld getWorld(File directory);

    public void close() {
        if (executor != null) {
            executor.setInactive();
            executor.await();
            executor = null;
        }
    }

}
