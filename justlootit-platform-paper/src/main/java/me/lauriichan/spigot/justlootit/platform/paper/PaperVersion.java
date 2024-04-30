package me.lauriichan.spigot.justlootit.platform.paper;

import org.bukkit.Bukkit;

import me.lauriichan.spigot.justlootit.platform.IVersion;
import me.lauriichan.spigot.justlootit.platform.util.MinecraftToPackageVersion;
import me.lauriichan.spigot.justlootit.platform.util.SimpleVersion;

public final class PaperVersion implements IVersion {
    
    public static final PaperVersion INSTANCE = new PaperVersion();

    public final SimpleVersion minecraftVersion = SimpleVersion.of(Bukkit.getServer().getMinecraftVersion());
    public final String packageVersion = MinecraftToPackageVersion.getPackageVersion(minecraftVersion);
    public final String craftbukkitPackage = Bukkit.getServer().getClass().getPackage().getName() + ".%s";

    private PaperVersion() {}

    @Override
    public String packageVersion() {
        return packageVersion;
    }

    @Override
    public String craftClassPath(String path) {
        return craftbukkitPackage.formatted(path);
    }

}
