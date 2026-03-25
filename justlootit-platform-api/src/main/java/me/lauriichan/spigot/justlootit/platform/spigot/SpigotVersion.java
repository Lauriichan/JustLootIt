package me.lauriichan.spigot.justlootit.platform.spigot;

import org.bukkit.Bukkit;

import me.lauriichan.spigot.justlootit.platform.IVersion;

public final class SpigotVersion implements IVersion {

    public static final SpigotVersion INSTANCE = new SpigotVersion();

    public final String packageVersion;
    public final String craftbukkitPackage;

    private SpigotVersion() {
        String[] pkgParts = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",");
        String cbPkg = Bukkit.getServer().getClass().getPackage().getName() + ".%s";
        String pkgVer;
        if (pkgParts.length > 3) {
            pkgVer = pkgParts[3];
            cbPkg = String.format("org.bukkit.craftbukkit.%s.%s", pkgVer, "%s");
        } else {
            pkgVer = "v26_1";
        }
        this.packageVersion = pkgVer;
        this.craftbukkitPackage = cbPkg;
    }

    @Override
    public String packageVersion() {
        return packageVersion;
    }

    @Override
    public String craftClassPath(String path) {
        return craftbukkitPackage.formatted(path);
    }

}
