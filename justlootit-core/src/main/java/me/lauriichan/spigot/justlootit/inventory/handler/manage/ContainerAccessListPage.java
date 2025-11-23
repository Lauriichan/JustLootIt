package me.lauriichan.spigot.justlootit.inventory.handler.manage;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.inventory.ChestSize;
import me.lauriichan.minecraft.pluginbase.inventory.ClickType;
import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventory;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.minecraft.pluginbase.inventory.paged.PageContext;
import me.lauriichan.minecraft.pluginbase.util.math.InventoryMath;
import me.lauriichan.spigot.justlootit.JustLootItKey;
import me.lauriichan.spigot.justlootit.capability.ActorCapability;
import me.lauriichan.spigot.justlootit.config.data.RefreshGroup;
import me.lauriichan.spigot.justlootit.data.Container;
import me.lauriichan.spigot.justlootit.data.Container.Access;
import me.lauriichan.spigot.justlootit.inventory.ItemHelper;
import me.lauriichan.spigot.justlootit.inventory.ItemHelper.PageType;
import me.lauriichan.spigot.justlootit.message.UIInventoryNames;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.storage.Stored;
import me.lauriichan.spigot.justlootit.util.DataHelper;
import me.lauriichan.spigot.justlootit.util.SimpleDataType;

@Extension
public final class ContainerAccessListPage extends ContainerPage {

    private static final String ATTR_PAGE = "AccessPage";
    private static final String ATTR_MAX_PAGE = "AccessMaxPage";

    @Override
    public void onPageUpdate(PageContext<ContainerPage, PlayerAdapter> context, boolean changed) {
        Actor<Player> actor = ActorCapability.actor(context.player());
        IGuiInventory inventory = context.inventory();
        @SuppressWarnings("unchecked")
        Stored<Container> storedContainer = inventory.attr(ContainerPageHandler.ATTR_CONTAINER, Stored.class).cast();
        if (inventory
            .updater().chestSize(ChestSize.GRID_5x9).title(actor
                .getTranslatedMessageAsString(UIInventoryNames.CONTAINER_MANAGE_PAGE_ACCESSES_NAME, Key.of("id", storedContainer.id())))
            .apply()) {
            return;
        }
        Container container = storedContainer.value();
        inventory.fill(ItemEditor.of(Material.BLACK_STAINED_GLASS_PANE).setName("&r"));
        inventory.clearSection(1, 1, 1, 7);
        inventory.clearSection(2, 1, 2, 7);

        int amount = container.accessAmount();
        int maxPage = Math.max(Math.floorDiv(amount, 14), 1);
        if (amount > 14 && amount % 14 != 0) {
            maxPage++;
        }
        inventory.attrSet(ATTR_MAX_PAGE, maxPage);
        int page = Math.min(inventory.attrOrDefault(ATTR_PAGE, Number.class, 1).intValue(), maxPage);
        inventory.attrSet(ATTR_PAGE, page);
        if (page > 1) {
            if (page != 2) {
                inventory.set(4, 1, ItemHelper.firstPage(actor));
            }
            inventory.set(4, 2, ItemHelper.pageCount(ItemHelper.previousPage(actor), page, maxPage, PageType.PREVIOUS));
        }
        if (page != maxPage) {
            if (page + 1 != maxPage) {
                inventory.set(4, 7, ItemHelper.pageCount(ItemHelper.lastPage(actor), page, maxPage, PageType.LAST));
            }
            inventory.set(4, 6, ItemHelper.pageCount(ItemHelper.nextPage(actor), page, maxPage, PageType.NEXT));
        }
        inventory.set(4, 4, ItemHelper.pageCount(ItemHelper.currentPage(actor, page, maxPage), page, maxPage, PageType.CURRENT));

        int endIdx = page * 14;
        int startIdx = (page - 1) * 14;
        int tmpIdx = 0;
        ObjectIterator<Entry<UUID, Access>> iterator = container.accesses().iterator();
        Server server = Bukkit.getServer();
        while (tmpIdx++ != startIdx) {
            if (!iterator.hasNext()) {
                return;
            }
            iterator.next();
        }
        OffsetDateTime now = OffsetDateTime.now();
        RefreshGroup group = container.group(context.player().asBukkit().getWorld());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(actor.getTranslatedMessageAsString(UIInventoryNames.GENERAL_DATE_FORMAT));
        int slotIndex = 0;
        Key time;
        OfflinePlayer player;
        for (int index = (page - 1) * 14; index < endIdx; index++, slotIndex++) {
            if (!iterator.hasNext()) {
                break;
            }
            Entry<UUID, Access> entry = iterator.next();
            player = server.getPlayer(entry.getKey());
            if (player == null) {
                player = server.getOfflinePlayer(entry.getKey());
            }
            time = Key.of("time", group == null ? actor.getTranslatedMessageAsString(UIInventoryNames.GENERAL_TIME_NEVER)
                : DataHelper.formTimeString(actor, group.duration(entry.getValue().time(), now)));
            ItemEditor editor = ItemEditor.of(Material.PLAYER_HEAD)
                .setName(actor.getTranslatedMessageAsString(UIInventoryNames.CONTAINER_MANAGE_PAGE_ACCESSES_ITEM_PLAYER_NAME,
                    Key.of("player", player.getName())))
                .lore()
                .set(actor.getTranslatedMessageAsString(UIInventoryNames.CONTAINER_MANAGE_PAGE_ACCESSES_ITEM_PLAYER_LORE,
                    Key.of("id", entry.getKey().toString()), Key.of("date", formatter.format(entry.getValue().time())),
                    Key.of("access.count", entry.getValue().accessCount()), time).split("\n"))
                .apply().applyItemMeta(
                    meta -> meta.getPersistentDataContainer().set(JustLootItKey.identity(), SimpleDataType.UUID, entry.getKey()));
            int row = 1 + Math.floorDiv(slotIndex, 7), column = 1 + slotIndex % 7;
            editor.applyHeadTexture(player, item -> inventory.set(row, column, item));
            inventory.set(row, column, editor);
        }
    }

    @Override
    public boolean onClickPickup(PageContext<ContainerPage, PlayerAdapter> context, ItemStack item, int slot, int amount, boolean cursor,
        ClickType type) {
        if (item.getType() != Material.PLAYER_HEAD) {
            return true;
        }
        IGuiInventory inventory = context.inventory();
        if (slot == InventoryMath.toSlot(4, 1, 9)) {
            inventory.attrSet(ATTR_PAGE, 1);
            inventory.update();
            return true;
        }
        if (slot == InventoryMath.toSlot(4, 2, 9)) {
            inventory.attrSet(ATTR_PAGE, inventory.attr(ATTR_PAGE, Number.class).intValue() - 1);
            inventory.update();
            return true;
        }
        if (slot == InventoryMath.toSlot(4, 6, 9)) {
            inventory.attrSet(ATTR_PAGE, inventory.attr(ATTR_PAGE, Number.class).intValue() + 1);
            inventory.update();
            return true;
        }
        if (slot == InventoryMath.toSlot(4, 7, 9)) {
            inventory.attrSet(ATTR_PAGE, inventory.attr(ATTR_MAX_PAGE, Number.class).intValue());
            inventory.update();
            return true;
        }
        UUID id = item.getItemMeta().getPersistentDataContainer().getOrDefault(JustLootItKey.identity(), SimpleDataType.UUID, null);
        if (id == null) {
            return true;
        }
        @SuppressWarnings("unchecked")
        Stored<Container> stored = inventory.attr(ContainerPageHandler.ATTR_CONTAINER, Stored.class);
        if (type == ClickType.SHIFT_LEFT) {
            stored.value().resetAccess(id);
        } else if (type == ClickType.LEFT || type == ClickType.RIGHT) {
            stored.value().decreaseAccessCount(id);
        }
        inventory.update();
        return true;
    }

    @Override
    public boolean onInventoryClose(PageContext<ContainerPage, PlayerAdapter> context) {
        context.openPage(ContainerOverviewPage.class);
        IGuiInventory inventory = context.inventory();
        inventory.attrUnset(ATTR_PAGE);
        inventory.attrUnset(ATTR_MAX_PAGE);
        return true;
    }

}
