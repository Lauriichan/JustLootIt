package me.lauriichan.spigot.justlootit.storage.tick;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractTickTimer {

    private static final long SEC_IN_NANOS = TimeUnit.SECONDS.toNanos(1);
    private static final long MILLI_IN_NANOS = TimeUnit.MILLISECONDS.toNanos(1);

    private final AtomicInteger state = new AtomicInteger(0);

    private volatile Thread timerThread;
    private volatile int tps;
    private volatile int tpm;
    
    private volatile long length = MILLI_IN_NANOS * 50L;
    private volatile long pauseLength = MILLI_IN_NANOS * 250L;
    
    public final void setPauseLength(long pauseLength, TimeUnit unit) {
        this.pauseLength = Math.max(unit.toNanos(pauseLength), MILLI_IN_NANOS * 10L);
    }
    
    public final long getPauseLength() {
        return pauseLength;
    }

    public final void setLength(long length, TimeUnit unit) {
        this.length = Math.max(unit.toNanos(length), MILLI_IN_NANOS);
    }
    
    public final long getLength() {
        return length;
    }
    
    public final int getTicksPerMinute() {
        return tpm;
    }
    
    public final int getTicksPerSecond() {
        return tps;
    }
    
    public final boolean isAlive() {
        return timerThread != null;
    }

    public final boolean isPaused() {
        return state.get() == 2;
    }

    public final void pause() {
        state.compareAndSet(1, 2);
    }

    public final void start() {
        if (timerThread != null) {
            state.compareAndSet(2, 1);
            return;
        }
        state.compareAndSet(0, 1);
        timerThread = new Thread(this::tickThread);
        timerThread.setDaemon(true);
        timerThread.setName("TickTimer");
        timerThread.start();
    }

    public final void stop() {
        if (timerThread == null) {
            return;
        }
        // This will shutdown the thread
        state.set(0);
        timerThread.interrupt();
        timerThread = null;
    }

    private final void tickThread() {
        long nextLength = this.length;
        long tickMillis;
        int tickNanos;
        long cycles;
        long prevNanoTime = System.nanoTime();
        long nanoTime = prevNanoTime;
        long delta = 0;
        long elapsed = 0;
        int counter = 0;
        int secondTick = 0;
        int secondCounter = 0;
        int currentState;
        while (true) {
            if ((currentState = state.get()) == 0) {
                break;
            }
            try {
                if (currentState == 2) {
                    Thread.sleep(this.pauseLength);
                    continue;
                }
                prevNanoTime = nanoTime;
                nanoTime = System.nanoTime();
                delta = nanoTime - prevNanoTime;
                elapsed += delta;
                tick(delta);
                if(elapsed >= SEC_IN_NANOS) {
                    elapsed = SEC_IN_NANOS - elapsed;
                    this.tps = counter;
                    secondCounter += counter;
                    counter = 0;
                    if(++secondTick == 60) {
                        this.tpm = secondCounter;
                        secondCounter = 0;
                        secondTick = 0;
                    }
                }
                nextLength = this.length;
                tickMillis = TimeUnit.NANOSECONDS.toMillis(nextLength - delta);
                tickNanos = (int) (nextLength - delta - TimeUnit.MILLISECONDS.toNanos(tickMillis));
                if (tickMillis > 2) {
                    Thread.sleep(tickMillis, tickNanos);
                    continue;
                }
                cycles = (TimeUnit.MILLISECONDS.toNanos(tickMillis) + tickNanos) / 2;
                while (cycles-- >= 0) {
                    Thread.yield();
                }
            } catch (InterruptedException e) {
                continue;
            }
        }
    }

    protected abstract void tick(long delta);

}
