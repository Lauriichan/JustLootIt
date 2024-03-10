package me.lauriichan.spigot.justlootit.platform.scheduler;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import me.lauriichan.laylib.logger.ISimpleLogger;

public abstract class Task<E> {

    private static final Object NULL = new Object();

    protected final ISimpleLogger logger;
    private final boolean repeating;

    private final AtomicReference<TaskState> state = new AtomicReference<>(TaskState.IDLE);
    private final AtomicReference<Object> value = new AtomicReference<>();

    private final ObjectArrayFIFOQueue<Stage<E>> stages = new ObjectArrayFIFOQueue<>();

    public Task(final ISimpleLogger logger, final boolean repeating) {
        this.logger = logger;
        this.repeating = repeating;
    }

    public final ISimpleLogger logger() {
        return logger;
    }

    public final boolean repeating() {
        return repeating;
    }

    public final TaskState state() {
        TaskState state = this.state.get();
        if (state.completed()) {
            return state;
        }
        if (isCancelled()) {
            this.state.set(TaskState.CANCELLED);
            return TaskState.CANCELLED;
        }
        return state;
    }

    public final boolean completed() {
        return state().completed();
    }

    public final boolean cancelled() {
        return state() == TaskState.CANCELLED;
    }

    @SuppressWarnings("unchecked")
    public final E value() {
        Object object = value.get();
        if (object == NULL) {
            return null;
        }
        return (E) object;
    }

    public final void complete(E value) {
        if (isCompleted()) {
            return;
        }
        state.set(TaskState.DONE);
        try {
            if (value == null) {
                this.value.set(NULL);
                return;
            }
            this.value.set(value);
        } finally {
            runStages();
        }
    }

    public final void cancel() {
        if (isCompleted()) {
            return;
        }
        this.state.set(TaskState.CANCELLED);
        try {
            doCancel();
        } finally {
            runStages();
        }
    }

    public final E join() {
        pushStage(new Stage.JoinStage<>());
        return value();
    }

    public final Task<E> thenRun(Runnable runnable) {
        pushStage(new Stage.RunStage<>(runnable));
        return this;
    }

    public final Task<E> thenConsume(Consumer<E> consumer) {
        pushStage(new Stage.ConsumeStage<>(consumer));
        return this;
    }

    private void pushStage(Stage<E> stage) {
        if (isCompleted()) {
            return;
        }
        stages.enqueue(stage);
        stage.start(this);
    }

    protected final boolean isCompleted() {
        if (this.state.get().completed()) {
            return true;
        }
        if (isCancelled()) {
            this.state.set(TaskState.CANCELLED);
            return true;
        }
        return false;
    }

    /*
     * Implementation
     */

    protected final void runStages() {
        synchronized (stages) {
            if (stages.isEmpty()) {
                return;
            }
            E value = value();
            while (!stages.isEmpty()) {
                try {
                    stages.dequeue().done(value);
                } catch (RuntimeException ignore) {
                    logger.error("Failed to run task completion stage", ignore);
                }
            }
        }
    }

    protected abstract void doCancel();

    protected abstract boolean isCancelled();

}
