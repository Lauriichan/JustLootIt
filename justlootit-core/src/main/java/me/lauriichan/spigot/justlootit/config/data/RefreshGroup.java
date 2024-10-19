package me.lauriichan.spigot.justlootit.config.data;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RefreshGroup {
    
    public static enum UniqueType {
        
        GLOBAL,
        TRUE,
        FALSE;
        
    }

    private final String id;

    private UniqueType unique = UniqueType.GLOBAL;
    private long timeoutMillis;
    private TimeUnit unit = TimeUnit.MILLISECONDS;
    private boolean incremental;

    public RefreshGroup(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public TimeUnit unit() {
        return unit;
    }

    public boolean incremental() {
        return incremental;
    }

    public void incremental(boolean incremental) {
        this.incremental = incremental;
    }
    
    public UniqueType unique() {
        return unique;
    }
    
    public void unique(UniqueType unique) {
        if (unique == null) {
            unique = UniqueType.GLOBAL;
        }
        this.unique = unique;
    }

    public void set(long timeoutTime, TimeUnit unit) {
        this.unit = Objects.requireNonNull(unit);
        timeoutTime(timeoutTime);
    }

    public void unit(TimeUnit unit) {
        unit = Objects.requireNonNull(unit);
        if (this.unit == unit) {
            return;
        }
        long time = timeoutTime();
        this.unit = unit;
        timeoutTime(time);
    }

    public long timeoutTime() {
        return unit.convert(timeoutMillis, TimeUnit.MILLISECONDS);
    }

    public void timeoutTime(long timeoutTime) {
        timeoutTime = unit.toMillis(timeoutTime);
        if (timeoutTime == Long.MAX_VALUE) {
            // Do conversion to actual millis to prevent any desync between config and this value
            timeoutTime = unit.toMillis(unit.convert(timeoutTime, TimeUnit.MILLISECONDS));
        }
        this.timeoutMillis = Math.max(timeoutTime, 0);
    }

    public long timeoutMillis() {
        return timeoutMillis;
    }

    public boolean isAccessible(OffsetDateTime time, OffsetDateTime now) {
        return time == null || timeoutMillis != 0 && time.plus(timeoutMillis, ChronoUnit.MILLIS).isBefore(now);
    }

    public Duration duration(OffsetDateTime time, OffsetDateTime now) {
        if (time == null) {
            return Duration.ZERO;
        }
        final Duration duration = Duration.between(now, time.plus(timeoutMillis, ChronoUnit.MILLIS));
        return duration.isNegative() ? Duration.ZERO : duration;
    }
}
