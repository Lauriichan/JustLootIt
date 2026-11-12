package me.lauriichan.spigot.justlootit.compatibility.provider.worldguard;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.JustLootItPermission;

public final class WorldGuardAccess implements IWorldGuardAccess {

    private static record RegisteredFlag(StateFlag flag, boolean success) {}

    private final ISimpleLogger logger;

    private final WorldGuard worldGuard;
    private final WorldGuardPlugin plugin;

    private final RegisteredFlag accessFlag, destroyFlag;

    private RegionContainer regionContainer;

    public WorldGuardAccess(ISimpleLogger logger, Plugin plugin) {
        this.logger = logger;
        this.plugin = (WorldGuardPlugin) plugin;
        this.worldGuard = WorldGuard.getInstance();

        this.accessFlag = registerFlag("jli-container-access", true);
        this.destroyFlag = registerFlag("jli-container-destruction", true);
    }

    private RegisteredFlag registerFlag(String name, boolean defaultValue) {
        boolean success = true;
        StateFlag flag = new StateFlag(name, defaultValue);
        try {
            worldGuard.getFlagRegistry().register(flag);
        } catch (FlagConflictException | IllegalStateException exp) {
            success = false;
            logger.warning("Failed to register WorldGuard flag", exp);
        }
        return new RegisteredFlag(flag, success);
    }

    public final void init() {
        if (regionContainer != null) {
            return;
        }
        this.regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
    }

    @Override
    public boolean canAccess(Player player, Location location) {
        if (!accessFlag.success() || player.hasPermission(JustLootItPermission.ADMIN_BYPASS)) {
            return true;
        }
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        return regionContainer.get(localPlayer.getWorld())
            .getApplicableRegions(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
            .testState(localPlayer, accessFlag.flag());
    }

    @Override
    public boolean canDestroy(Player player, Location location) {
        if (!destroyFlag.success() || player.hasPermission(JustLootItPermission.ADMIN_BYPASS)) {
            return true;
        }
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        return regionContainer.get(localPlayer.getWorld())
            .getApplicableRegions(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
            .testState(localPlayer, destroyFlag.flag());
    }

}
