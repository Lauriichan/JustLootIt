package me.lauriichan.spigot.justlootit.nms.convert;

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

public final class ProtoExecutor implements Executor {

    private static final int MAX_THRESHOLD = Integer.MAX_VALUE / 2;
    private static ProtoExecutor INSTANCE;

    static final ProtoExecutor get(ISimpleLogger logger) {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        return new ProtoExecutor(logger);
    }

    private final PriorityQueue<Runnable> queue = PriorityQueues.synchronize(new ObjectArrayFIFOQueue<>());
    private final ObjectList<ProtoThread> threads;

    private final ISimpleLogger logger;

    private volatile boolean active = true;

    public ProtoExecutor(ISimpleLogger logger) {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException();
        }
        INSTANCE = this;
        this.logger = logger;
        int available = Runtime.getRuntime().availableProcessors();
        ProtoThread[] threads = new ProtoThread[Math.max((int) Math.floor(available * 0.8), 4)];
        for (int i = 0; i < threads.length; i++) {
            ProtoThread thread = new ProtoThread(i, this);
            threads[i] = thread;
            thread.start();
        }
        logger.info("Conversion will run with {0} threads.", threads.length);
        if (threads.length > available) {
            logger.warning("There are less cpus available ({0}) than allocated, this might slow down the conversion a little.", available);
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
    
    public ObjectList<ProtoThread> threads() {
        return threads;
    }
    
    public boolean isQueueEmpty() {
        return queue.isEmpty();
    }

    final void runQueue() {
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

    void setInactive() {
        active = false;
    }

    void await() {
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
