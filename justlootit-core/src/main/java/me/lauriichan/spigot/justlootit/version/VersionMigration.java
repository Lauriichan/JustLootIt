package me.lauriichan.spigot.justlootit.version;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.extension.ExtensionPoint;
import me.lauriichan.minecraft.pluginbase.extension.IExtension;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.platform.PlatformType;
import me.lauriichan.spigot.justlootit.platform.version.SimpleVersion;

@ExtensionPoint
public abstract class VersionMigration implements IExtension {

    private final int id;
    private final String message;

    public VersionMigration(int id, String message) {
        this.id = id;
        this.message = message;
    }
    
    public final int id() {
        return id;
    }

    public final String message() {
        return message;
    }

    public abstract boolean applies(PlatformType platformType, SimpleVersion last, SimpleVersion current);

    public abstract void run(ISimpleLogger logger, JustLootItPlugin plugin);

}
