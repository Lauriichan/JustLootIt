package me.lauriichen.spigot.justlootit.platform.folia;

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import me.lauriichan.spigot.justlootit.platform.Scheduler;

public final class FoliaScheduler extends Scheduler {
    
    private final Plugin plugin;
    
    private final GlobalRegionScheduler global;
    private final RegionScheduler region;
    private final AsyncScheduler async;
    
    public FoliaScheduler(Plugin plugin) {
        this.plugin = plugin;
        final Server server = plugin.getServer();
        this.global = server.getGlobalRegionScheduler();
        this.region = server.getRegionScheduler();
        this.async = server.getAsyncScheduler();
    }
    
    @Override
    public void entity(Entity entity, Runnable runnable) {
        entity.getScheduler().run(plugin, (t) -> runnable.run(), null);
    }
    
    @Override
    public void entityLater(Entity entity, Runnable runnable, long delayTicks) {
        entity.getScheduler().runDelayed(plugin, (t) -> runnable.run(), null, delayTicks);
    }
    
    @Override
    public void entityRepeat(Entity entity, Runnable runnable, long delayTicks, long repeatTicks) {
        entity.getScheduler().runAtFixedRate(plugin, (t) -> runnable.run(), null, delayTicks, repeatTicks);
    }
    
    @Override
    public void regional(Location location, Runnable runnable) {
        region.run(plugin, location, (t) -> runnable.run());
    }
    
    @Override
    public void regionalLater(Location location, Runnable runnable, long delayTicks) {
        region.runDelayed(plugin, location, (t) -> runnable.run(), delayTicks);
    }
    
    @Override
    public void regionalRepeat(Location location, Runnable runnable, long delayTicks, long repeatTicks) {
        region.runAtFixedRate(plugin, location, (t) -> runnable.run(), delayTicks, repeatTicks);
    }

    @Override
    public void sync(Runnable runnable) {
        global.run(plugin, (t) -> runnable.run());
    }

    @Override
    public void syncLater(Runnable runnable, long delayTicks) {
        global.runDelayed(plugin, (t) -> runnable.run(), delayTicks);
    }

    @Override
    public void syncRepeat(Runnable runnable, long delayTicks, long repeatTicks) {
        global.runAtFixedRate(plugin, (t) -> runnable.run(), delayTicks, repeatTicks);
    }

    @Override
    public void async(Runnable runnable) {
        async.runNow(plugin, (t) -> runnable.run());
    }

    @Override
    public void asyncLater(Runnable runnable, long delayTicks) {
        async.runDelayed(plugin, (t) -> runnable.run(), delayTicks * 50L, TimeUnit.MILLISECONDS);
    }

    @Override
    public void asyncRepeat(Runnable runnable, long delayTicks, long repeatTicks) {
        async.runAtFixedRate(plugin, (t) -> runnable.run(), delayTicks * 50L, repeatTicks * 50L, TimeUnit.MILLISECONDS);
    }

}
