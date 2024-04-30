package me.lauriichan.spigot.justlootit.platform.spigot;

import org.bukkit.Bukkit;

import me.lauriichan.spigot.justlootit.platform.IVersion;

public final class SpigotVersion implements IVersion {
    
    public static final SpigotVersion INSTANCE = new SpigotVersion();

    public final String packageVersion = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    public final String craftbukkitPackage = String.format("org.bukkit.craftbukkit.%s.%s", packageVersion, "%s");

    private SpigotVersion() {}

    @Override
    public String packageVersion() {
        return packageVersion;
    }

    @Override
    public String craftClassPath(String path) {
        return craftbukkitPackage.formatted(path);
    }

}
