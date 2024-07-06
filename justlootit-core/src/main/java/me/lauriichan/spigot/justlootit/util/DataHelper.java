package me.lauriichan.spigot.justlootit.util;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.laylib.localization.MessageProvider;
import me.lauriichan.spigot.justlootit.JustLootItKey;
import me.lauriichan.spigot.justlootit.message.Messages;
import me.lauriichan.spigot.justlootit.util.persistence.BreakData;

public final class DataHelper {

    public static final long DAY_IN_SECONDS = TimeUnit.DAYS.toSeconds(1);
    public static final long HOUR_IN_SECONDS = TimeUnit.HOURS.toSeconds(1);
    public static final long MINUTE_IN_SECONDS = TimeUnit.MINUTES.toSeconds(1);

    public static final long MILLISECOND_IN_NANOSECONDS = TimeUnit.MILLISECONDS.toNanos(1);
    public static final long MILLISECOND_IN_MICROSECONDS = TimeUnit.MILLISECONDS.toMicros(1);

    private DataHelper() {
        throw new UnsupportedOperationException();
    }

    public static boolean hasIdentity(PersistentDataContainer container) {
        return container.has(JustLootItKey.identity(), PersistentDataType.LONG);
    }

    public static boolean hasIdentityOrOffset(PersistentDataContainer container) {
        return container.has(JustLootItKey.identity(), PersistentDataType.LONG)
            || container.has(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR);
    }

    public static boolean canBreakContainer(PersistentDataContainer container, UUID uuid) {
        BreakData data = container.getOrDefault(JustLootItKey.breakData(), BreakData.BREAK_DATA_TYPE, null);
        OffsetDateTime now = OffsetDateTime.now();
        if (data == null) {
            container.set(JustLootItKey.breakData(), BreakData.BREAK_DATA_TYPE, new BreakData(uuid, now));
            return false;
        }
        if (!data.playerId().equals(uuid) || now.isBefore(data.time())) {
            container.set(JustLootItKey.breakData(), BreakData.BREAK_DATA_TYPE, new BreakData(uuid, now.plusMinutes(2)));
            return false;
        }
        container.remove(JustLootItKey.breakData());
        return true;
    }

    public static String formTimeString(Actor<?> actor, Duration duration) {
        long second = duration.getSeconds();
        long minute = 0, hour = 0, day = 0;
        MessageProvider provider = Messages.CONTAINER_TIME_FORMAT_SECONDS;
        if (DAY_IN_SECONDS + HOUR_IN_SECONDS <= second) {
            day = Math.floorDiv(second, DAY_IN_SECONDS);
            hour = Math.floorDiv(second -= day * DAY_IN_SECONDS, HOUR_IN_SECONDS);
            minute = Math.floorDiv(second -= hour * HOUR_IN_SECONDS, MINUTE_IN_SECONDS);
            second -= minute * MINUTE_IN_SECONDS;
            provider = Messages.CONTAINER_TIME_FORMAT_DAYS;
        } else if (HOUR_IN_SECONDS + MINUTE_IN_SECONDS <= second) {
            hour = Math.floorDiv(second -= day * DAY_IN_SECONDS, HOUR_IN_SECONDS);
            minute = Math.floorDiv(second -= hour * HOUR_IN_SECONDS, MINUTE_IN_SECONDS);
            second -= minute * MINUTE_IN_SECONDS;
            provider = Messages.CONTAINER_TIME_FORMAT_HOURS;
        } else if (MINUTE_IN_SECONDS + 1 <= second) {
            minute = Math.floorDiv(second -= hour * HOUR_IN_SECONDS, MINUTE_IN_SECONDS);
            second -= minute * MINUTE_IN_SECONDS;
            provider = Messages.CONTAINER_TIME_FORMAT_MINUTES;
        }
        return actor.getTranslatedMessageAsString(provider,
            Key.of("milliseconds",
                actor.getTranslatedMessageAsString(Messages.CONTAINER_TIME_UNIT_MILLISECOND,
                    Key.of("value", Math.floorDiv(duration.getNano(), MILLISECOND_IN_NANOSECONDS)))),
            Key.of("seconds", actor.getTranslatedMessageAsString(Messages.CONTAINER_TIME_UNIT_SECOND, Key.of("value", second))),
            Key.of("minutes", actor.getTranslatedMessageAsString(Messages.CONTAINER_TIME_UNIT_MINUTE, Key.of("value", minute))),
            Key.of("hours", actor.getTranslatedMessageAsString(Messages.CONTAINER_TIME_UNIT_HOUR, Key.of("value", hour))),
            Key.of("days", actor.getTranslatedMessageAsString(Messages.CONTAINER_TIME_UNIT_DAY, Key.of("value", day))));
    }

    public static MessageProvider providerByUnit(TimeUnit unit) {
        switch (unit) {
        case SECONDS:
            return Messages.CONTAINER_TIME_UNIT_SECOND;
        case MINUTES:
            return Messages.CONTAINER_TIME_UNIT_MINUTE;
        case HOURS:
            return Messages.CONTAINER_TIME_UNIT_HOUR;
        case DAYS:
            return Messages.CONTAINER_TIME_UNIT_DAY;
        case MILLISECONDS:
        default:
            return Messages.CONTAINER_TIME_UNIT_MILLISECOND;
        }
    }

    public static long unsupportedToMillis(long time, TimeUnit unit) {
        if (unit == TimeUnit.MICROSECONDS) {
            if (time < MILLISECOND_IN_MICROSECONDS) {
                return 0;
            }
            return Math.floorDiv(time, MILLISECOND_IN_MICROSECONDS);
        } else if (unit == TimeUnit.NANOSECONDS) {
            if (time < MILLISECOND_IN_NANOSECONDS) {
                return 0;
            }
            return Math.floorDiv(time, MILLISECOND_IN_NANOSECONDS);
        }
        throw new UnsupportedOperationException("Unit is supported");
    }

}
