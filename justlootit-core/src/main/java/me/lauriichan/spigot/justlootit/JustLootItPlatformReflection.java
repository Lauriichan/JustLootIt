package me.lauriichan.spigot.justlootit;

import me.lauriichan.minecraft.pluginbase.IBukkitReflection;
import me.lauriichan.spigot.justlootit.platform.IVersion;
import me.lauriichan.spigot.justlootit.platform.JustLootItPlatform;

class JustLootItPlatformReflection implements IBukkitReflection {
    
    private final IVersion version;
    
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
