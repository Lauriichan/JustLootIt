package me.lauriichan.spigot.justlootit.platform.spigot;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.platform.JustLootItPlatform;
import me.lauriichan.spigot.justlootit.platform.PlatformType;
import me.lauriichan.spigot.justlootit.platform.version.ServerVersion;
import me.lauriichan.spigot.justlootit.platform.version.SimpleVersion;

public final class SpigotPlatform extends JustLootItPlatform {

    private final SpigotScheduler scheduler;

    public SpigotPlatform(Plugin plugin, ISimpleLogger logger) {
        super(plugin, logger);
        this.scheduler = new SpigotScheduler(plugin, logger);
    }

    @Override
    protected ServerVersion detectVersion() {
        String versionStr = Bukkit.getServer().getVersion();
        int mcVerStart = versionStr.indexOf("(MC:");
        if (mcVerStart == -1) {
            return null;
        }
        mcVerStart += 5;
        SimpleVersion minecraftVersion = SimpleVersion.of(versionStr.substring(mcVerStart, versionStr.indexOf(')', mcVerStart)).trim());
        String craftBukkitPackage;
        if (minecraftVersion.major() < 26) {
            String[] pkgParts = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",");
            if (pkgParts.length <= 3) {
                return null;
            }
            craftBukkitPackage = String.format("org.bukkit.craftbukkit.%s.%s", pkgParts[3], "%s");
        } else {
            craftBukkitPackage = Bukkit.getServer().getClass().getPackage().getName() + ".%s";
        }
        return new ServerVersion(craftBukkitPackage, minecraftVersion);
    }

    @Override
    public SpigotScheduler scheduler() {
        return scheduler;
    }

    @Override
    public PlatformType type() {
        return PlatformType.SPIGOT;
    }

}
