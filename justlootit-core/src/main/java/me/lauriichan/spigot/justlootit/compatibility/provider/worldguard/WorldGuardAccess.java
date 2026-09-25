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

    private final WorldGuardPlugin plugin;
    private final StateFlag flag;

    private final boolean flagSuccess;

    private RegionContainer regionContainer;

    public WorldGuardAccess(ISimpleLogger logger, Plugin plugin) {
        this.plugin = (WorldGuardPlugin) plugin;
        this.flag = new StateFlag("jli-container-access", true);
        boolean flagSuccess = true;
        try {
            WorldGuard.getInstance().getFlagRegistry().register(flag);
        } catch (FlagConflictException | IllegalStateException exp) {
            flagSuccess = false;
            logger.warning("Failed to register WorldGuard flag", exp);
        }
        this.flagSuccess = flagSuccess;
    }

    public final void init() {
        if (regionContainer != null) {
            return;
        }
        this.regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
    }

    @Override
    public boolean canAccess(Player player, Location location) {
        if (!flagSuccess || player.hasPermission(JustLootItPermission.ADMIN_BYPASS)) {
            return true;
        }
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        return regionContainer.get(localPlayer.getWorld())
            .getApplicableRegions(BlockVector3.at(location.getX(), location.getY(), location.getZ())).testState(localPlayer, flag);
    }

}
