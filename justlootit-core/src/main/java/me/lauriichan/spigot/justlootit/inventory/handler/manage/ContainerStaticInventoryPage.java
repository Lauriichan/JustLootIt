package me.lauriichan.spigot.justlootit.inventory.handler.manage;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.inventory.ChestSize;
import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventory;
import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventoryUpdater;
import me.lauriichan.minecraft.pluginbase.inventory.paged.PageContext;
import me.lauriichan.spigot.justlootit.capability.ActorCapability;
import me.lauriichan.spigot.justlootit.data.Container;
import me.lauriichan.spigot.justlootit.data.ContainerType;
import me.lauriichan.spigot.justlootit.data.StaticContainer;
import me.lauriichan.spigot.justlootit.message.UIInventoryNames;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;

@Extension
public final class ContainerStaticInventoryPage extends ContainerPage {
    
    // TODO: Make editable

    private static final ChestSize[] SIZES = ChestSize.values();

    @Override
    public void onPageOpen(PageContext<ContainerPage, PlayerAdapter> context) {
        Container container = context.inventory().attr(ContainerPageHandler.ATTR_CONTAINER, Container.class);
        if (container == null || container.type() != ContainerType.STATIC) {
            context.openPage(ContainerOverviewPage.class);
            return;
        }
        super.onPageOpen(context);
    }

    @Override
    public void onPageUpdate(PageContext<ContainerPage, PlayerAdapter> context, boolean changed) {
        Actor<Player> actor = ActorCapability.actor(context.player());
        IGuiInventory inventory = context.inventory();
        StaticContainer container = inventory.attr(ContainerPageHandler.ATTR_CONTAINER, StaticContainer.class);
        if (updateInventory(inventory, actor, container.amount(), container.id())) {
            return;
        }
        container.loadTo(inventory.getInventory());
    }

    private boolean updateInventory(IGuiInventory inventory, Actor<Player> actor, int size, long id) {
        IGuiInventoryUpdater updater = inventory.updater().chestSize(ChestSize.GRID_3x9)
            .title(actor.getTranslatedMessageAsString(UIInventoryNames.CONTAINER_MANAGE_PAGE_INVENTORY_NAME, Key.of("id", id)));
        if (size == 3) {
            updater.type(InventoryType.DROPPER);
        } else if (size == 5) {
            updater.type(InventoryType.HOPPER);
        } else if (size >= 9) {
            int rows = Math.min(Math.floorDiv(size, 9), 6);
            while ((size - rows * 9) > 0 && rows < 6) {
                rows++;
            }
            updater.chestSize(SIZES[rows - 1]);
        }
        return updater.apply();
    }

    @Override
    public boolean onInventoryClose(PageContext<ContainerPage, PlayerAdapter> context) {
        context.openPage(ContainerOverviewPage.class);
        return true;
    }

}
