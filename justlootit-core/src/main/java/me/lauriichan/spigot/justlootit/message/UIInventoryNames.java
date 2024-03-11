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

    @Message(id = "inventory.container-manage.page.overview.item.container-info.vanilla.name", content = "&cInformation")
    public static MessageProvider CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_VANILLA_NAME;
    @Message(id = "inventory.container-manage.page.overview.item.container-info.vanilla.lore", content = {
        "&7Seed: &c$seed",
        "&7LootTable: &c$lootTable"
    })
    public static MessageProvider CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_VANILLA_LORE;

    @Message(id = "inventory.container-manage.page.inventory.name", content = "&7Container (&c$id&7): &fInventory")
    public static MessageProvider CONTAINER_MANAGE_PAGE_INVENTORY_NAME;

    @Message(id = "inventory.container-manage.page.accesses.name", content = "&7Container (&c$id&7): &fAccesses")
    public static MessageProvider CONTAINER_MANAGE_PAGE_ACCESSES_NAME;

    @Message(id = "inventory.container-manage.page.accesses.item.player.name", content = "&c$player")
    public static MessageProvider CONTAINER_MANAGE_PAGE_ACCESSES_ITEM_PLAYER_NAME;
    @Message(id = "inventory.container-manage.page.accesses.item.player.lore", content = {
        "&r",
        "&8Id: &7$id",
        "&r",
        "&8Last access:",
        "&8= &7$date",
        "&r",
        "&8Next possible access:",
        "&8= &7$time",
        "&r",
        "&7Click to reset access"
    })
    public static MessageProvider CONTAINER_MANAGE_PAGE_ACCESSES_ITEM_PLAYER_LORE;

}
