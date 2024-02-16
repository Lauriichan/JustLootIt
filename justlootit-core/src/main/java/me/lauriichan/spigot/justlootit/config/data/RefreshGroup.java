package me.lauriichan.spigot.justlootit.config.data;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RefreshGroup {
    
    private final String id;
    
    private long timeoutTime;
    private TimeUnit unit = TimeUnit.MILLISECONDS;
    
    public RefreshGroup(String id) {
        this.id = id;
    }
    
    public String id() {
        return id;
    }
    
    public TimeUnit unit() {
        return unit;
    }
    
    public void unit(TimeUnit unit) {
        this.unit = Objects.requireNonNull(unit);
    }
    
    public long timeoutTime() {
        return unit.convert(timeoutTime, TimeUnit.MILLISECONDS);
    }
    
    public void timeoutTime(long timeoutTime) {
        timeoutTime = unit.toMillis(timeoutTime);
        if (timeoutTime == Long.MAX_VALUE) {
            // Do conversion to actual millis to prevent any desync between config and this value
            timeoutTime = unit.toMillis(unit.convert(timeoutTime, TimeUnit.MILLISECONDS));
        }
        this.timeoutTime = Math.max(timeoutTime, 0);
    }
    
    public long timeoutMillis() {
        return timeoutTime;
    }

    public boolean isAccessible(OffsetDateTime time, OffsetDateTime now) {
        return time == null || timeoutTime != 0 && time.plus(timeoutTime, ChronoUnit.MILLIS).isBefore(now);
    }
    
    public Duration duration(OffsetDateTime time, OffsetDateTime now) {
        if (time == null) {
            return Duration.ZERO;
        }
        final Duration duration = Duration.between(now, time.plus(timeoutTime, ChronoUnit.MILLIS));
        return duration.isNegative() ? Duration.ZERO : duration;
    }
}
