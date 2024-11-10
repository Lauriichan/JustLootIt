package me.lauriichan.spigot.justlootit.storage.util.executor;

import java.util.NoSuchElementException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.PriorityQueues;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.laylib.logger.ISimpleLogger;

public final class ProtoExecutor<T extends Thread> implements Executor {

    @FunctionalInterface
    public static interface ThreadInfo {

        void informThreads(ISimpleLogger logger, int cpuCount, int threadCount);

    }

    public static interface ThreadSupplier<T extends Thread> {

        T createThread(int id, ProtoExecutor<T> executor);

        T[] createArray(int size);

    }

    private static final int MAX_THRESHOLD = Integer.MAX_VALUE / 2;

    private final PriorityQueue<Runnable> queue = PriorityQueues.synchronize(new ObjectArrayFIFOQueue<>());
    private final ObjectList<T> threads;

    private final ISimpleLogger logger;

    private volatile boolean active = true;

    public ProtoExecutor(ISimpleLogger logger, ThreadSupplier<T> supplier) {
        this(logger, supplier, null);
    }

    public ProtoExecutor(ISimpleLogger logger, ThreadSupplier<T> supplier, ThreadInfo info) {
        this.logger = logger;
        int available = Runtime.getRuntime().availableProcessors();
        T[] threads = supplier.createArray(Math.max((int) Math.floor(available * 0.8), 4));
        for (int i = 0; i < threads.length; i++) {
            T thread = supplier.createThread(i, this);
            threads[i] = thread;
            thread.start();
        }
        if (info != null) {
            info.informThreads(logger, available, threads.length);
        }
        this.threads = ObjectLists.unmodifiable(ObjectArrayList.wrap(threads));
    }

    @Override
    public void execute(Runnable runnable) {
        if (!active) {
            throw new RejectedExecutionException("Executor is not active anymore");
        }
        while (queue.size() > MAX_THRESHOLD) {
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
            }
        }
        queue.enqueue(runnable);
    }

    public ObjectList<T> threads() {
        return threads;
    }

    public boolean isQueueEmpty() {
        return queue.isEmpty();
    }

    public void runQueue() {
        while (active) {
            while (!queue.isEmpty()) {
                try {
                    queue.dequeue().run();
                } catch (NoSuchElementException ignore) {
                } catch (Throwable exp) {
                    logger.warning("Failed to execute runnable.", exp);
                }
            }
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
            }
        }
    }

    public void setInactive() {
        active = false;
    }

    public void await() {
        for (Thread thread : threads) {
            while (thread.isAlive()) {
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                }
            }
        }
    }

}
