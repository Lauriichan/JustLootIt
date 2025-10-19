package me.lauriichan.spigot.justlootit.util;

import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import me.lauriichan.minecraft.pluginbase.util.attribute.Attributable;
import me.lauriichan.spigot.justlootit.storage.util.counter.CounterProgress;

public final class ProgressNotifier extends Attributable {
    
    public static interface IProgressNotifier {

        void notify(CounterProgress progress, long elapsed, boolean detailed);

    }

    public static interface IDoneNotifier {

        void notify(CounterProgress progress, long elapsed);

    }

    public static final DecimalFormat PERCENTAGE_FORMAT = new DecimalFormat("0.00%");

    public static String asPercentage(CounterProgress progress) {
        return PERCENTAGE_FORMAT.format(progress.counter().progress());
    }

    public static String asPercentage(double progress) {
        return PERCENTAGE_FORMAT.format(progress);
    }

    private CounterProgress progress;

    private volatile long waitTimeout = -1;
    private volatile long detailedTimeout = -1;

    private IDoneNotifier doneNotifier;
    private IProgressNotifier progressNotifier;

    public final CounterProgress progress() {
        return progress;
    }

    public final ProgressNotifier progress(CounterProgress progress) {
        if (this.progress != null && !this.progress.isDone()) {
            throw new IllegalStateException("Can't replace an ongoing process.");
        }
        this.progress = progress;
        return this;
    }
    
    public IDoneNotifier doneNotifier() {
        return doneNotifier;
    }
    
    public ProgressNotifier doneNotifier(IDoneNotifier doneNotifier) {
        this.doneNotifier = doneNotifier;
        return this;
    }
    
    public IProgressNotifier progressNotifier() {
        return progressNotifier;
    }
    
    public ProgressNotifier progressNotifier(IProgressNotifier progressNotifier) {
        this.progressNotifier = progressNotifier;
        return this;
    }

    public long waitTimeout() {
        return waitTimeout;
    }

    public ProgressNotifier waitTimeout(TimeUnit unit, long waitTimeout) {
        return waitTimeout(unit.toMillis(waitTimeout));
    }

    public ProgressNotifier waitTimeout(long waitTimeout) {
        this.waitTimeout = Math.max(waitTimeout, 100);
        return this;
    }

    public long detailedTimeout() {
        return detailedTimeout;
    }

    public ProgressNotifier detailedTimeout(TimeUnit unit, long detailedInfoTimeout) {
        return detailedTimeout(unit.toMillis(detailedInfoTimeout));
    }

    public ProgressNotifier detailedTimeout(long detailedInfoTimeout) {
        this.detailedTimeout = Math.max(detailedInfoTimeout, -1);
        return this;
    }

    public void await() {
        CounterProgress progress = this.progress;
        IDoneNotifier doneNotifier = this.doneNotifier;
        IProgressNotifier progressNotifier = this.progressNotifier;
        if (progress == null) {
            throw new IllegalArgumentException("Progress not defined");
        }
        if (waitTimeout == -1) {
            throw new IllegalArgumentException("Wait timeout not defined");
        }
        if (progress.isDone()) {
            return;
        }
        long now = System.currentTimeMillis(), dumpTime = now, start = now;
        boolean detailed;
        loop:
        while (true) {
            detailed = false;
            if (detailedTimeout != -1 && now > dumpTime + detailedTimeout) {
                dumpTime = now;
                detailed = true;
            }
            if (progressNotifier != null) {
                progressNotifier.notify(progress, now - start, detailed);
            }
            while (progress.future().isDone()) {
                if (!progress.next()) {
                    break loop;
                }
                dumpTime = now;
            }
            try {
                progress.future().get(waitTimeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                continue;
            } finally {
                now = System.currentTimeMillis();
            }
        }
        if (doneNotifier != null) {
            doneNotifier.notify(progress, now - start);
        }
    }

}
