package me.lauriichan.spigot.justlootit.platform;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public abstract class Scheduler {

    public void shutdown() {}

    public void entity(final Entity entity, final Runnable runnable) {
        sync(runnable);
    }

    public void entityLater(final Entity entity, final Runnable runnable, final long delayTicks) {
        syncLater(runnable, delayTicks);
    }

    public void entityRepeat(final Entity entity, final Runnable runnable, final long delayTicks, final long repeatTicks) {
        syncRepeat(runnable, delayTicks, repeatTicks);
    }

    public void regional(final Location location, final Runnable runnable) {
        sync(runnable);
    }

    public void regionalLater(final Location location, final Runnable runnable, final long delayTicks) {
        syncLater(runnable, delayTicks);
    }

    public void regionalRepeat(final Location location, final Runnable runnable, final long delayTicks, final long repeatTicks) {
        syncRepeat(runnable, delayTicks, repeatTicks);
    }

    public abstract void sync(final Runnable runnable);

    public abstract void syncLater(final Runnable runnable, final long delayTicks);

    public abstract void syncRepeat(final Runnable runnable, final long delayTicks, final long repeatTicks);

    public abstract void async(final Runnable runnable);

    public abstract void asyncLater(final Runnable runnable, final long delayTicks);

    public abstract void asyncRepeat(final Runnable runnable, final long delayTicks, final long repeatTicks);

}
