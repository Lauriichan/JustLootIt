package me.lauriichan.spigot.justlootit.platform.paper;

import org.bukkit.Bukkit;

import me.lauriichan.spigot.justlootit.platform.IVersion;
import me.lauriichan.spigot.justlootit.platform.util.MinecraftToPackageVersion;
import me.lauriichan.spigot.justlootit.platform.util.SimpleVersion;

public final class PaperVersion implements IVersion {

    public static final PaperVersion INSTANCE = new PaperVersion();

    public final SimpleVersion minecraftVersion = SimpleVersion.of(Bukkit.getServer().getMinecraftVersion());
    public final String packageVersion, coreVersion;
    public final String craftbukkitPackage = Bukkit.getServer().getClass().getPackage().getName() + ".%s";

    private PaperVersion() {
        String pkgVer = MinecraftToPackageVersion.getPackageVersion(minecraftVersion);
        String coreVer = pkgVer;
        if (pkgVer == null && minecraftVersion.major() >= 26) {
            pkgVer = "paper.v26_1";
            coreVer = "26_1";
        }
        this.packageVersion = pkgVer;
        this.coreVersion = coreVer;
    }

    @Override
    public String packageVersion() {
        return packageVersion;
    }

    @Override
    public String coreVersion() {
        return coreVersion;
    }

    @Override
    public String craftClassPath(String path) {
        return craftbukkitPackage.formatted(path);
    }

}
