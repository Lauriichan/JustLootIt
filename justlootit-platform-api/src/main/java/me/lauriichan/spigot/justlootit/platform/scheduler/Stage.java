package me.lauriichan.spigot.justlootit.platform.scheduler;

import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.function.Function;

abstract class Stage<E> {

    void start(Task<E> task) {}

    abstract void done(E value);

    public static class JoinStage<E> extends Stage<E> {

        private volatile Thread thread;

        public JoinStage() {
            this.thread = Thread.currentThread();
        }

        @Override
        public void done(E value) {
            if (this.thread == null) {
                return;
            }
            LockSupport.unpark(thread);
            this.thread = null;
        }

        @Override
        public void start(Task<E> task) {
            LockSupport.park();
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
        public void done(E value) {
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
        public void done(E value) {
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
        public void done(E value) {
            if (this.runnable == null) {
                return;
            }
            this.runnable.run();
            this.runnable = null;
        }

    }

}
