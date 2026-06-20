package me.lauriichan.spigot.justlootit;

import me.lauriichan.minecraft.pluginbase.IBukkitReflection;
import me.lauriichan.spigot.justlootit.platform.JustLootItPlatform;
import me.lauriichan.spigot.justlootit.platform.version.ServerVersion;

class JustLootItPlatformReflection implements IBukkitReflection {

    private final ServerVersion version;

    public JustLootItPlatformReflection(final JustLootItPlatform platform) {
        this.version = platform.version();
    }

    @Override
    public String createCraftBukkitPath(String path) {
        return version.craftClassPath(path);
    }

    @Override
    public String createMinecraftPath(String path) {
        return "net.minecraft.%s".formatted(path);
    }

}
