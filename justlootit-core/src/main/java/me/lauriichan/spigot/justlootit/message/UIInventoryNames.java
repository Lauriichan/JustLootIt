package me.lauriichan.spigot.justlootit.message;

import me.lauriichan.laylib.localization.MessageProvider;
import me.lauriichan.laylib.localization.source.Message;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.message.IMessageExtension;

@Extension
public final class UIInventoryNames implements IMessageExtension {

    private UIInventoryNames() {
        throw new UnsupportedOperationException();
    }

    /*
     * General
     */

    @Message(id = "inventory.general.page.next", content = "&cNext page >>")
    public static MessageProvider GENERAL_PAGE_NEXT;
    @Message(id = "inventory.general.page.previous", content = "&c<< Previous page")
    public static MessageProvider GENERAL_PAGE_PREVIOUS;
    @Message(id = "inventory.general.page.last", content = "&cLast page >>>")
    public static MessageProvider GENERAL_PAGE_LAST;
    @Message(id = "inventory.general.page.first", content = "&c<<< First page")
    public static MessageProvider GENERAL_PAGE_FIRST;
    @Message(id = "inventory.general.page.current", content = "&7Page: &c$current &8/ &7$max")
    public static MessageProvider GENERAL_PAGE_CURRENT;
    @Message(id = "inventory.general.page.back", content = "&7<= Back")
    public static MessageProvider GENERAL_PAGE_BACK;

    @Message(id = "inventory.general.format.date", content = "&7HH&8:&7mm&8:&7ss &8- &7dd&8/&7MM&8/&7y")
    public static MessageProvider GENERAL_DATE_FORMAT;
    @Message(id = "inventory.general.format.time-never", content = "&cnever")
    public static MessageProvider GENERAL_TIME_NEVER;

    /*
     * Loot UI
     */

    @Message(id = "inventory.loot-ui.name", content = "&cLoot Inventory")
    public static MessageProvider LOOT_UI_NAME;

    /*
     * Container Manage UI
     */

    @Message(id = "inventory.container-manage.page.overview.name", content = "&7Container (&c$id&7): &fOverview")
    public static MessageProvider CONTAINER_MANAGE_PAGE_OVERVIEW_NAME;

    @Message(id = "inventory.container-manage.page.overview.item.access.name", content = "&7Accesses &8(&c$amount&8)")
    public static MessageProvider CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_ACCESS_NAME;
    @Message(id = "inventory.container-manage.page.overview.item.access.lore", content = {
        "&r",
        "&7Click to open"
    })
    public static MessageProvider CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_ACCESS_LORE;

    @Message(id = "inventory.container-manage.page.overview.item.refresh-group.name", content = "&7Refresh group")
    public static MessageProvider CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_REFRESH_GROUP_NAME;
    @Message(id = "inventory.container-manage.page.overview.item.refresh-group.lore", content = {
        "&8= &c$id"
    })
    public static MessageProvider CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_REFRESH_GROUP_LORE;

    @Message(id = "inventory.container-manage.page.overview.item.container-info.frame.name", content = "&7Frame item")
    public static MessageProvider CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_FRAME_NAME;

    @Message(id = "inventory.container-manage.page.overview.item.container-info.static.name", content = "&7Contents &8(&c$size&8)")
    public static MessageProvider CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_STATIC_NAME;
    @Message(id = "inventory.container-manage.page.overview.item.container-info.static.lore", content = {
        "&r",
        "&7Click to view"
    })
    public static MessageProvider CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_STATIC_LORE;

    @Message(id = "inventory.container-manage.page.overview.item.container-info.vanilla.name", content = "&7Information")
    public static MessageProvider CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_VANILLA_NAME;
    @Message(id = "inventory.container-manage.page.overview.item.container-info.vanilla.lore", content = {
        "&r",
        "&8Seed:",
        "&8= &7$seed",
        "&r",
        "&8LootTable:",
        "&8= &7$lootTable"
    })
    public static MessageProvider CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_VANILLA_LORE;

    @Message(id = "inventory.container-manage.page.overview.item.container-info.custom.name", content = "&7Information")
    public static MessageProvider CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_CUSTOM_NAME;
    @Message(id = "inventory.container-manage.page.overview.item.container-info.custom.lore", content = {
        "&r",
        "&8Seed:",
        "&8= &7$seed",
        "&r",
        "&8LootTable:",
        "&8= &7$lootTable"
    })
    public static MessageProvider CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_CUSTOM_LORE;

    @Message(id = "inventory.container-manage.page.overview.item.container-info.compatibility.name", content = "&7Information")
    public static MessageProvider CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_COMPATIBILITY_NAME;
    @Message(id = "inventory.container-manage.page.overview.item.container-info.compatibility.lore.format", content = {
        "&r",
        "&8$key:",
        "&8= &7$value"
    })
    public static MessageProvider CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_COMPATIBILITY_LORE_FORMAT;
    @Message(id = "inventory.container-manage.page.overview.item.container-info.compatibility.lore.multi-format.header", content = {
        "&r",
        "&8$key:",
    })
    public static MessageProvider CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_COMPATIBILITY_LORE_MULTI_FORMAT_HEADER;
    @Message(id = "inventory.container-manage.page.overview.item.container-info.compatibility.lore.multi-format.entry", content = {
        "&8= &7$value"
    })
    public static MessageProvider CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_COMPATIBILITY_LORE_MULTI_FORMAT_ENTRY;
    @Message(id = "inventory.container-manage.page.overview.item.container-info.compatibility.lore.no-data-available", content = {
        "&r",
        "&8No data available"
    })
    public static MessageProvider CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_COMPATIBILITY_LORE_NO_DATA_AVAILABLE;

    @Message(id = "inventory.container-manage.page.inventory.name", content = "&7Container (&c$id&7): &fInventory")
    public static MessageProvider CONTAINER_MANAGE_PAGE_INVENTORY_NAME;

    @Message(id = "inventory.container-manage.page.accesses.name", content = "&7Container (&c$id&7): &fAccesses")
    public static MessageProvider CONTAINER_MANAGE_PAGE_ACCESSES_NAME;

    @Message(id = "inventory.container-manage.page.accesses.item.player.name", content = "&c$player")
    public static MessageProvider CONTAINER_MANAGE_PAGE_ACCESSES_ITEM_PLAYER_NAME;
    @Message(id = "inventory.container-manage.page.accesses.item.player.lore", content = {
        "&r",
        "&8Id: &7$id",
        "&8Access Count: &7$access.count",
        "&r",
        "&8Last access:",
        "&8= &7$date",
        "&r",
        "&8Next possible access:",
        "&8= &7$time",
        "&r",
        "&7Click to decrease access count",
        "&7Shift click to reset access"
    })
    public static MessageProvider CONTAINER_MANAGE_PAGE_ACCESSES_ITEM_PLAYER_LORE;

    /*
     * Loot Table Viewer UI
     */
    @Message(id = "inventory.loottable-viewer.page.chooser.name", content = "&cChoose container item")
    public static MessageProvider LOOTTABLE_VIEWER_PAGE_CHOOSER_NAME;
    @Message(id = "inventory.loottable-viewer.page.chooser.back.lore", content = {
        "&7Click to go back to path:",
        "&c$path"
    })
    public static MessageProvider LOOTTABLE_VIEWER_PAGE_CHOOSER_BACK_LORE;

    @Message(id = "inventory.loottable-viewer.page.chooser.container-item.name", content = "$#plugin.name &d$container.type &7container")
    public static MessageProvider LOOTTABLE_VIEWER_PAGE_CHOOSER_CONTAINER_ITEM_NAME;
    @Message(id = "inventory.loottable-viewer.page.chooser.container-item.lore", content = {
        "",
        "&8Container Type: &7$container.type",
        "&8Loot Table Key: &7$key"
    })
    public static MessageProvider LOOTTABLE_VIEWER_PAGE_CHOOSER_CONTAINER_ITEM_LORE;

    @Message(id = "inventory.loottable-viewer.page.browser.name", content = "&7Loot Table Browser (&c$type&7)")
    public static MessageProvider LOOTTABLE_VIEWER_PAGE_BROWSER_NAME;

    @Message(id = "inventory.loottable-viewer.page.browser.back.lore", content = {
        "&7Click to go back to '&c$name&7'",
        "",
        "&8Current path:",
        "&8$path"
    })
    public static MessageProvider LOOTTABLE_VIEWER_PAGE_BROWSER_BACK_LORE;

    @Message(id = "inventory.loottable-viewer.page.browser.tab.unselected.name", content = "&7$name")
    public static MessageProvider LOOTTABLE_VIEWER_PAGE_BROWSER_TAB_UNSELECTED_NAME;
    @Message(id = "inventory.loottable-viewer.page.browser.tab.unselected.lore", content = "")
    public static MessageProvider LOOTTABLE_VIEWER_PAGE_BROWSER_TAB_UNSELECTED_LORE;
    @Message(id = "inventory.loottable-viewer.page.browser.tab.selected.name", content = "&d$name")
    public static MessageProvider LOOTTABLE_VIEWER_PAGE_BROWSER_TAB_SELECTED_NAME;
    @Message(id = "inventory.loottable-viewer.page.browser.tab.selected.lore", content = {
        "&5Selected"
    })
    public static MessageProvider LOOTTABLE_VIEWER_PAGE_BROWSER_TAB_SELECTED_LORE;

    @Message(id = "inventory.loottable-viewer.page.browser.entry.category.name", content = "&7$name")
    public static MessageProvider LOOTTABLE_VIEWER_PAGE_BROWSER_ENTRY_CATEGORY_NAME;
    @Message(id = "inventory.loottable-viewer.page.browser.entry.category.lore", content = {
        "&7Click to open category",
        "",
        "&8$path"
    })
    public static MessageProvider LOOTTABLE_VIEWER_PAGE_BROWSER_ENTRY_CATEGORY_LORE;

    @Message(id = "inventory.loottable-viewer.page.browser.entry.table.name", content = "&e$name")
    public static MessageProvider LOOTTABLE_VIEWER_PAGE_BROWSER_ENTRY_TABLE_NAME;
    @Message(id = "inventory.loottable-viewer.page.browser.entry.table.lore", content = {
        "&7Click to get placeable container",
        "",
        "&8$path"
    })
    public static MessageProvider LOOTTABLE_VIEWER_PAGE_BROWSER_ENTRY_TABLE_LORE;

}
