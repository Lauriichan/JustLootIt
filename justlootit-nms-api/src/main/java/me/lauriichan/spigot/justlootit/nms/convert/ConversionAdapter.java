package me.lauriichan.spigot.justlootit.nms.convert;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ConversionAdapter {
    
    private volatile ExecutorService workerPool;
    private volatile int workerCount;
    
    protected final ExecutorService workerPool() {
        if (workerPool != null) {
            return workerPool;
        }
        return workerPool = Executors.newCachedThreadPool(this::createWorkerThread);
    }
    
    private final Thread createWorkerThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setName("JLI Conversion Worker " + (workerCount++));
        return thread;
    }
    
    public abstract ProtoWorld getWorld(File directory);

}
