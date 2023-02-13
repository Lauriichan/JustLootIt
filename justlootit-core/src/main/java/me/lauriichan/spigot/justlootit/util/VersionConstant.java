package me.lauriichan.spigot.justlootit.util;

import org.bukkit.Bukkit;

public final class VersionConstant {

    private VersionConstant() {
        throw new UnsupportedOperationException("Constant class");
    }

    public static final String PACKAGE_VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    public static final String CRAFTBUKKIT_PACKAGE = String.format("org.bukkit.craftbukkit.%s.%s", PACKAGE_VERSION, "%s");

    public static String craftClassPath(final String path) {
        return String.format(CRAFTBUKKIT_PACKAGE, path);
    }

}