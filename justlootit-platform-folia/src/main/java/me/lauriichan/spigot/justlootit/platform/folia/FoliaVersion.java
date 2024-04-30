package me.lauriichan.spigot.justlootit.platform.folia;

import org.bukkit.Bukkit;

import me.lauriichan.spigot.justlootit.platform.IVersion;
import me.lauriichan.spigot.justlootit.platform.util.MinecraftToPackageVersion;
import me.lauriichan.spigot.justlootit.platform.util.SimpleVersion;

public final class FoliaVersion implements IVersion {
    
    public static final FoliaVersion INSTANCE = new FoliaVersion();

    public final SimpleVersion minecraftVersion = SimpleVersion.of(Bukkit.getServer().getMinecraftVersion());
    public final String packageVersion = MinecraftToPackageVersion.getPackageVersion(minecraftVersion);
    public final String craftbukkitPackage = Bukkit.getServer().getClass().getPackage().getName() + ".%s";

    private FoliaVersion() {}

    @Override
    public String packageVersion() {
        return packageVersion;
    }

    @Override
    public String craftClassPath(String path) {
        return craftbukkitPackage.formatted(path);
    }

}
