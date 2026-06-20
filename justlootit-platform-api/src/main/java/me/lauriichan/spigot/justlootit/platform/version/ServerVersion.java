package me.lauriichan.spigot.justlootit.platform.version;

public record ServerVersion(String craftBukkitPackage, SimpleVersion minecraftVersion) {

    public String craftClassPath(String path) {
        return craftBukkitPackage.formatted(path);
    }

}
