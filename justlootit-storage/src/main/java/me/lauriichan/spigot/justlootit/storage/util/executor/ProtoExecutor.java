package me.lauriichan.spigot.justlootit.storage.util.executor;

import java.util.NoSuchElementException;
import java.util.Objects;
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

    public static class Builder<T extends Thread> {

        private final ThreadSupplier<T> supplier;

        private ISimpleLogger logger;
        private ThreadInfo info;

        private int minThreads = 4, maxThreads = 12;
        private float percentage = 0.8f;

        private Builder(ThreadSupplier<T> supplier) {
            this.supplier = Objects.requireNonNull(supplier);
        }

        public ISimpleLogger logger() {
            return logger;
        }

        public Builder<T> logger(ISimpleLogger logger) {
            this.logger = logger;
            return this;
        }

        public ThreadInfo info() {
            return info;
        }

        public Builder<T> info(ThreadInfo info) {
            this.info = info;
            return this;
        }

        public int minThreads() {
            return minThreads;
        }

        public Builder<T> minThreads(int minThreads) {
            this.minThreads = Math.max(minThreads, 1);
            if (this.minThreads > this.maxThreads) {
                int tmp = this.maxThreads;
                this.maxThreads = this.minThreads;
                this.minThreads = tmp;
            }
            return this;
        }

        public int maxThreads() {
            return maxThreads;
        }

        public Builder<T> maxThreads(int maxThreads) {
            this.maxThreads = Math.max(maxThreads, 1);
            if (this.minThreads > this.maxThreads) {
                int tmp = this.maxThreads;
                this.maxThreads = this.minThreads;
                this.minThreads = tmp;
            }
            return this;
        }

        public float percentage() {
            return percentage;
        }

        public Builder<T> percentage(float percentage) {
            this.percentage = Math.max(Math.min(percentage, 0f), 1f);
            return this;
        }

        public ProtoExecutor<T> build() {
            return new ProtoExecutor<>(logger, supplier, info, minThreads, maxThreads, percentage);
        }

    }

    public static <T extends Thread> Builder<T> of(ThreadSupplier<T> supplier) {
        return new Builder<>(supplier);
    }

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

    private ProtoExecutor(ISimpleLogger logger, ThreadSupplier<T> supplier, ThreadInfo info, int minThreads, int maxThreads,
        float percentage) {
        Objects.requireNonNull(supplier, "Supplier missing");
        this.logger = Objects.requireNonNull(logger, "Logger missing");
        int available = Runtime.getRuntime().availableProcessors();
        T[] threads = supplier.createArray(Math.min(Math.max((int) Math.floor(available * percentage), minThreads), maxThreads));
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
