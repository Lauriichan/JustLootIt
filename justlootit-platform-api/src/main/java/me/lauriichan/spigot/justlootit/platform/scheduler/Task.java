package me.lauriichan.spigot.justlootit.platform.scheduler;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import me.lauriichan.laylib.logger.ISimpleLogger;

public abstract class Task<E> {
    
    private static record Result(Throwable throwable) {}

    private static final Result NULL = new Result(null);

    protected final ISimpleLogger logger;
    private final boolean repeating;

    private final AtomicReference<TaskState> state = new AtomicReference<>(TaskState.IDLE);
    private volatile Object value;

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
    public final E value() throws IllegalStateException {
        if (value == null) {
            throw new IllegalStateException("Task isn't complete yet");
        }
        if (value instanceof Result) {
            return null;
        }
        return (E) this.value;
    }

    @SuppressWarnings("unchecked")
    public final E join() {
        if (isCompleted()) {
            return value();
        }
        pushStage(new Stage.JoinStage<>());
        Object value = VALUE.getVolatile(this);
        if (value instanceof Result) {
            return null;
        }
        return (E) value;
    }

    public final void complete(E value) {
        if (isCompleted()) {
            return;
        }
        try {
            if (value == null) {
                this.value = NULL;
                return;
            }
            this.value = value;
        } finally {
            state.set(TaskState.DONE);
            runStages(value);
        }
    }

    public final void cancel() {
        if (isCompleted()) {
            return;
        }
        this.state.set(TaskState.CANCELLED);
        try {
            this.value = NULL;
            doCancel();
        } finally {
            runStages(null);
        }
    }

    public final Task<E> thenRun(Runnable runnable) {
        pushStage(new Stage.RunStage<>(runnable));
        return this;
    }

    public final <T> Task<T> thenMap(Function<E, T> mapper) {
        SimpleTask<T> task = new SimpleTask<>(logger);
        pushStage(new Stage.MapStage<>(mapper, task));
        return task;
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

    protected final void runStages(E value) {
        synchronized (stages) {
            if (stages.isEmpty()) {
                return;
            }
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
    
    // VarHandle mechanics
    private static final VarHandle VALUE;
    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            VALUE = lookup.findVarHandle(Task.class, "value", Object.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

}
