package me.lauriichan.spigot.justlootit.inventory.handler.manage;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.inventory.ChestSize;
import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventory;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.minecraft.pluginbase.inventory.paged.PageContext;
import me.lauriichan.minecraft.pluginbase.util.math.InventoryMath;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.data.*;
import me.lauriichan.spigot.justlootit.inventory.Textures;
import me.lauriichan.spigot.justlootit.message.UIInventoryNames;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;

@Extension
public final class ContainerOverviewPage extends ContainerPage {

    // TODO: Make frame item editable

    // TODO: Make vanilla lootable changable

    private final JustLootItPlugin plugin;

    public ContainerOverviewPage(JustLootItPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean defaultPage() {
        return true;
    }

    @Override
    public void onPageUpdate(PageContext<ContainerPage, PlayerAdapter> context, boolean changed) {
        Actor<Player> actor = plugin.actor(context.player().asBukkit());
        IGuiInventory inventory = context.inventory();
        Container container = inventory.attr(ContainerPageHandler.ATTR_CONTAINER, Container.class);
        if (inventory.updater().chestSize(ChestSize.GRID_4x9)
            .title(actor.getTranslatedMessageAsString(UIInventoryNames.CONTAINER_MANAGE_PAGE_OVERVIEW_NAME, Key.of("id", container.id())))
            .apply()) {
            return;
        }
        inventory.fill(ItemEditor.of(Material.BLACK_STAINED_GLASS_PANE).setName("&r"));
        inventory.set(0, 4, container.createIcon());
        switch (container.type()) {
        case FRAME:
            onFrameContainer((FrameContainer) container, inventory, actor);
            break;
        case STATIC:
            onStaticContainer((StaticContainer) container, inventory, actor);
            break;
        case VANILLA:
            onVanillaContainer((VanillaContainer) container, inventory, actor);
            break;
        default:
            break;
        }
        inventory.set(2, 4, ItemEditor.ofHead(Textures.GEODE_EXLAMATION_MARK)
            .setName(actor.getTranslatedMessageAsString(UIInventoryNames.CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_ACCESS_NAME,
                Key.of("amount", container.accessAmount())))
            .lore().set(actor.getTranslatedMessageAsString(UIInventoryNames.CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_ACCESS_LORE).split("\n"))
            .apply());
        String refreshGroupId = container.getGroupId();
        inventory.set(2, 7,
            ItemEditor.ofHead(Textures.GEODE_OCTOTHORPE)
                .setName(actor.getTranslatedMessageAsString(UIInventoryNames.CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_REFRESH_GROUP_NAME)).lore()
                .set(actor.getTranslatedMessageAsString(UIInventoryNames.CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_REFRESH_GROUP_LORE,
                    Key.of("id", refreshGroupId == null || refreshGroupId.isEmpty() ? "None" : refreshGroupId)).split("\n"))
                .apply());
    }

    private void onFrameContainer(FrameContainer container, IGuiInventory inventory, Actor<Player> actor) {
        inventory.set(1, 1, ItemEditor.ofHead(Textures.GEODE_QUESTION_MARK)
            .setName(actor.getTranslatedMessageAsString(UIInventoryNames.CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_FRAME_NAME)));
        inventory.set(2, 1, container.getItem().clone());
    }

    private void onStaticContainer(StaticContainer container, IGuiInventory inventory, Actor<Player> actor) {
        inventory.set(2, 1,
            ItemEditor.ofHead(Textures.GEODE_SETTINGS)
                .setName(actor.getTranslatedMessageAsString(UIInventoryNames.CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_STATIC_NAME,
                    Key.of("size", container.amount())))
                .lore()
                .set(actor.getTranslatedMessageAsString(UIInventoryNames.CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_STATIC_LORE)
                    .split("\n"))
                .apply());
    }

    private void onVanillaContainer(VanillaContainer container, IGuiInventory inventory, Actor<Player> actor) {
        inventory.set(2, 1, ItemEditor.ofHead(Textures.GEODE_BLANK)
            .setName(actor.getTranslatedMessageAsString(UIInventoryNames.CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_VANILLA_NAME))
            .lore().set(actor.getTranslatedMessageAsString(UIInventoryNames.CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_VANILLA_LORE,
                Key.of("seed", container.getSeed()), Key.of("lootTable", container.getLootTableKey())).split("\n"))
            .apply());
    }

    @Override
    public boolean onClickPickup(PageContext<ContainerPage, PlayerAdapter> context, ItemStack item, int slot, int amount, boolean cursor) {
        Container container = context.inventory().attr(ContainerPageHandler.ATTR_CONTAINER, Container.class);
        if (slot == InventoryMath.toSlot(2, 1, 9)) {
            ContainerType type = container.type();
            switch (type) {
            case STATIC:
                context.openPage(ContainerStaticInventoryPage.class);
                break;
            default:
                break;
            }
            return true;
        }
        if (slot == InventoryMath.toSlot(2, 4, 9)) {
            context.openPage(ContainerAccessListPage.class);
            return true;
        }
        return true;
    }

}
