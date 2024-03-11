package me.lauriichan.spigot.justlootit.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import me.lauriichan.minecraft.pluginbase.inventory.paged.PagedInventoryHandler;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;

public abstract class AbstractPageHandler<P extends IPage<P>> extends PagedInventoryHandler<P, PlayerAdapter> {

    private final VersionHandler versionHandler;
    
    public AbstractPageHandler(JustLootItPlugin plugin, Class<P> handlerType) {
        super(plugin, handlerType, PlayerAdapter.class);
        this.versionHandler = plugin.versionHandler();
    }

    @Override
    protected HumanEntity entityFromPlayer(PlayerAdapter player) {
        return player.asBukkit();
    }

    @Override
    protected boolean isSame(PlayerAdapter player1, PlayerAdapter player2) {
        return player1 == player2 || player1.getUniqueId().equals(player2.getUniqueId());
    }

    @Override
    protected PlayerAdapter playerFromEntity(HumanEntity entity) {
        if (entity instanceof Player player) {
            return versionHandler.getPlayer(player);
        }
        return versionHandler.getPlayer(entity.getUniqueId());
    }

}
