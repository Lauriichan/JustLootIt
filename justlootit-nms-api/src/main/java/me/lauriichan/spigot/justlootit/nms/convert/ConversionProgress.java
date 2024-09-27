package me.lauriichan.spigot.justlootit.nms.convert;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import me.lauriichan.spigot.justlootit.nms.util.counter.Counter;

public final class ConversionProgress {

    private final Counter counter;
    private final List<CompletableFuture<Void>> futures;
    
    private volatile int index = 0;

    public ConversionProgress(final Counter counter, final List<CompletableFuture<Void>> futures) {
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
    
    public boolean hasRegions() {
        return !futures.isEmpty();
    }
    
    public boolean hasNext() {
        return index + 1 < futures.size();
    }
    
    public void reset() {
        index = 0;
    }

    public boolean isDone() {
        return futures.stream().filter(future -> !future.isDone()).findAny().isPresent();
    }

}
