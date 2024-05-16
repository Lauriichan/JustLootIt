package me.lauriichan.spigot.justlootit.nms.convert;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class ConversionAdapter {

    private volatile ExecutorService workerPool;
    private volatile int workerCount;

    protected final ExecutorService workerPool() {
        if (workerPool != null) {
            return workerPool;
        }
        return workerPool = new ThreadPoolExecutor(4, 16, 20, TimeUnit.SECONDS, new PriorityBlockingQueue<>(16, (a, b) -> 0), this::createWorkerThread);
    }

    private final Thread createWorkerThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setName("justlootit-conversion-worker-" + (workerCount++));
        return thread;
    }

    public abstract ProtoWorld getWorld(File directory);

}
