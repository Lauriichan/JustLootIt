package me.lauriichan.spigot.justlootit.storage.util.executor;

import java.util.concurrent.CompletableFuture;
import java.lang.Runnable;

public final class FutureTask extends CompletableFuture<Void> implements Runnable {

    private final Runnable runnable;

    public FutureTask(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void run() {
        if (isDone()) {
            return;
        }
        try {
            runnable.run();
            complete(null);
        } catch (Throwable thr) {
            completeExceptionally(thr);
        }
    }

}
