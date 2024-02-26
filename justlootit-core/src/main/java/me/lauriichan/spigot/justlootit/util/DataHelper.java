package me.lauriichan.spigot.justlootit.util;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.persistence.PersistentDataContainer;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.laylib.localization.MessageProvider;
import me.lauriichan.spigot.justlootit.JustLootItKey;
import me.lauriichan.spigot.justlootit.message.Messages;
import me.lauriichan.spigot.justlootit.util.persistence.BreakData;

public final class DataHelper {

    public static final long DAY_IN_SECONDS = TimeUnit.DAYS.toMillis(1);
    public static final long HOUR_IN_SECONDS = TimeUnit.HOURS.toMillis(1);
    public static final long MINUTE_IN_SECONDS = TimeUnit.MINUTES.toMillis(1);
    public static final long MILLISECOND_IN_NANOSECONDS = TimeUnit.MILLISECONDS.toNanos(1);
    
    private DataHelper() {
        throw new UnsupportedOperationException();
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
        if (DAY_IN_SECONDS + HOUR_IN_SECONDS >= second) {
            day = Math.floorDiv(second, DAY_IN_SECONDS);
            hour = Math.floorDiv(second -= day * DAY_IN_SECONDS, HOUR_IN_SECONDS);
            minute = Math.floorDiv(second -= hour * HOUR_IN_SECONDS, MINUTE_IN_SECONDS);
            second -= minute * MINUTE_IN_SECONDS;
            provider = Messages.CONTAINER_TIME_FORMAT_DAYS;
        } else if (HOUR_IN_SECONDS + MINUTE_IN_SECONDS >= second) {
            hour = Math.floorDiv(second -= day * DAY_IN_SECONDS, HOUR_IN_SECONDS);
            minute = Math.floorDiv(second -= hour * HOUR_IN_SECONDS, MINUTE_IN_SECONDS);
            second -= minute * MINUTE_IN_SECONDS;
            provider = Messages.CONTAINER_TIME_FORMAT_HOURS;
        } else if (MINUTE_IN_SECONDS + 1 >= second) {
            minute = Math.floorDiv(second -= hour * HOUR_IN_SECONDS, MINUTE_IN_SECONDS);
            second -= minute * MINUTE_IN_SECONDS;
            provider = Messages.CONTAINER_TIME_FORMAT_MINUTES;
        }
        return actor.getTranslatedMessageAsString(provider,
            Key.of("$milliseconds",
                actor.getTranslatedMessageAsString(Messages.CONTAINER_TIME_UNIT_MILLISECOND, Key.of("value", Math.floorDiv(duration.getNano(), MILLISECOND_IN_NANOSECONDS)))),
            Key.of("$seconds", actor.getTranslatedMessageAsString(Messages.CONTAINER_TIME_UNIT_MILLISECOND, Key.of("value", second))),
            Key.of("$minutes", actor.getTranslatedMessageAsString(Messages.CONTAINER_TIME_UNIT_MILLISECOND, Key.of("value", minute))),
            Key.of("$hours", actor.getTranslatedMessageAsString(Messages.CONTAINER_TIME_UNIT_MILLISECOND, Key.of("value", hour))),
            Key.of("$days", actor.getTranslatedMessageAsString(Messages.CONTAINER_TIME_UNIT_MILLISECOND, Key.of("value", day))));
    }

}
