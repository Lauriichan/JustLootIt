package me.lauriichan.spigot.justlootit.nms.convert;

import java.io.File;

import me.lauriichan.laylib.logger.ISimpleLogger;

public abstract class ConversionAdapter implements AutoCloseable {

    private volatile ProtoExecutor executor;

    public final ProtoExecutor executor() {
        return executor;
    }
    
    protected final ProtoExecutor workerPool(ISimpleLogger logger) {
        if (executor != null) {
            return executor;
        }
        return executor = ProtoExecutor.get(logger);
    }

    public abstract ProtoWorld getWorld(File directory);
    
    public void close() {
        if (executor != null) {
            executor.setInactive();
            executor.await();
        }
    }

}
