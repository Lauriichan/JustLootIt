package me.lauriichan.spigot.justlootit.storage.util.counter;

import java.util.concurrent.CompletableFuture;

import it.unimi.dsi.fastutil.objects.ObjectList;

public final class CounterProgress {

    private final Counter counter;
    private final ObjectList<CompletableFuture<Void>> futures;
    
    private volatile int index = 0;

    public CounterProgress(final Counter counter, final ObjectList<CompletableFuture<Void>> futures) {
        this.counter = counter;
        this.futures = futures;
    }

    public Counter counter() {
        return counter;
    }
    
    public int futureCount() {
        return futures.size();
    }
    
    public int futureIndex() {
        return index;
    }
    
    public CompletableFuture<Void> future() {
        return futures.get(index);
    }
    
    public boolean next() {
        if (index + 1 >= futures.size()) {
            return false;
        }
        index += 1;
        return true;
    }
    
    public boolean hasFutures() {
        return !futures.isEmpty();
    }
    
    public boolean hasNext() {
        return index + 1 < futures.size();
    }
    
    public void reset() {
        index = 0;
    }

    public boolean isDone() {
        return futures.stream().noneMatch(future -> !future.isDone());
    }

}
