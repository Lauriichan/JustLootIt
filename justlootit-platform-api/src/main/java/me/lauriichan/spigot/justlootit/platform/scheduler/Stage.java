package me.lauriichan.spigot.justlootit.platform.scheduler;

import java.lang.invoke.VarHandle;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.function.Function;

import me.lauriichan.laylib.reflection.JavaLookup;

abstract class Stage<E> {

    void start(Task<E> task) {}

    abstract void done(Task<E> task, E value);

    public static class JoinStage<E> extends Stage<E> {
        
        private static final VarHandle THREAD_HANDLE;
        
        static {
            VarHandle threadHandle = null;
            try {
                threadHandle = JavaLookup.PLATFORM.lookup().findVarHandle(JoinStage.class, "thread", Thread.class);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // We don't really care, we know we have access, and we know the field exists.
            }
            THREAD_HANDLE = threadHandle;
        }
        
        @SuppressWarnings("unused")
        private volatile Thread thread = Thread.currentThread();

        @Override
        public void done(Task<E> task, E value) {
            Thread thr = (Thread) THREAD_HANDLE.getAndSet(this, null);
            if (thr != null) {
                LockSupport.unpark(thr);
            }
        }

        @Override
        public void start(Task<E> task) {
            if (task.isCompleted()) {
                return;
            }
            Thread thr = (Thread) THREAD_HANDLE.get(this);
            if (thr != null) {
                LockSupport.park(thr);
            }
        }

    }

    public static class MapStage<E, T> extends Stage<E> {

        private volatile Function<E, T> mapper;
        private volatile Task<T> targetTask;

        public MapStage(Function<E, T> mapper, Task<T> targetTask) {
            this.mapper = mapper;
            this.targetTask = targetTask;
        }

        @Override
        public void done(Task<E> task, E value) {
            if (this.mapper == null || this.targetTask == null) {
                return;
            }
            T targetValue = this.mapper.apply(value);
            this.mapper = null;
            this.targetTask.complete(targetValue);
            this.targetTask = null;
        }

    }

    public static class ConsumeStage<E> extends Stage<E> {

        private volatile Consumer<E> consumer;

        public ConsumeStage(Consumer<E> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void done(Task<E> task, E value) {
            if (this.consumer == null) {
                return;
            }
            this.consumer.accept(value);
            this.consumer = null;
        }

    }

    public static class RunStage<E> extends Stage<E> {

        private volatile Runnable runnable;

        public RunStage(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void done(Task<E> task, E value) {
            if (this.runnable == null) {
                return;
            }
            this.runnable.run();
            this.runnable = null;
        }

    }

}
