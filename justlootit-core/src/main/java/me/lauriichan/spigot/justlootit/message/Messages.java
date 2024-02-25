package me.lauriichan.spigot.justlootit.message;

import me.lauriichan.laylib.localization.MessageProvider;
import me.lauriichan.laylib.localization.source.Message;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.message.IMessageExtension;

@Extension
public final class Messages implements IMessageExtension {

    private Messages() {
        throw new UnsupportedOperationException();
    }

    /*
     * Plugin
     */

    @Message(id = "plugin.name", content = "&#2964EAJust&#EA2964Loot&#64EA29It")
    public static MessageProvider NAME;
    @Message(id = "plugin.prefix", content = "$#plugin.name &8|&7")
    public static MessageProvider PREFIX;

    /*
     * Commands
     */

    // System (general)

    @Message(id = "command.system.actor.not-supported", content = "$#plugin.prefix The command '&c$command&7' can only be excuted by actors of type &c$actorType&7.")
    public static MessageProvider COMMAND_SYSTEM_ACTOR_NOT_SUPPORTED;
    @Message(id = "command.system.actor.world-required", content = "$#plugin.prefix Please specify a world.")
    public static MessageProvider COMMAND_SYSTEM_ACTOR_WORLD_REQUIRED;

    @Message(id = "command.system.arrow.left", content = "&c<<")
    public static MessageProvider COMMAND_SYSTEM_ARROW_LEFT;
    @Message(id = "command.system.arrow.seperator", content = " &8/ ")
    public static MessageProvider COMMAND_SYSTEM_ARROW_SEPERATOR;
    @Message(id = "command.system.arrow.right", content = "&c>>")
    public static MessageProvider COMMAND_SYSTEM_ARROW_RIGHT;

    @Message(id = "command.system.page.previous", content = "&cPrevious page")
    public static MessageProvider COMMAND_SYSTEM_PAGE_PREVIOUS;
    @Message(id = "command.system.page.next", content = "&cNext page")
    public static MessageProvider COMMAND_SYSTEM_PAGE_NEXT;
    
    @Message(id = "command.system.error.storage-access.level", content = "$#plugin.prefix Failed to access storage of level '&c$level&7', please try again later.")
    public static MessageProvider COMMAND_SYSTEM_ERROR_STORAGE_ACCESS_LEVEL;
    @Message(id = "command.system.error.storage-access.player", content = "$#plugin.prefix Failed to access storage of player '&c$player&7', please try again later.")
    public static MessageProvider COMMAND_SYSTEM_ERROR_STORAGE_ACCESS_PLAYER;

    // Help

    @Message(id = "command.help.command.none", content = "$#plugin.prefix There are no commands to list that you are permitted to see!")
    public static MessageProvider COMMAND_HELP_NONE;
    @Message(id = "command.help.command.unknown", content = "$#plugin.prefix The command '&c$command&7' doesn't exist!")
    public static MessageProvider COMMAND_HELP_UNKNOWN;
    @Message(id = "command.help.command.overview-header", content = "Commands")
    public static MessageProvider COMMAND_HELP_OVERVIEW_HEADER;

    @Message(id = "command.help.header.format.start", content = {
        "$#plugin.name &8| &7$helpText &8[&c$page&8/&7$maxPage&8]",
        "&8[&7required&8] &8<&7optional&8>"
    })
    public static MessageProvider COMMAND_HELP_HEADER_FORMAT_START;
    @Message(id = "command.help.header.format.end", content = "$#plugin.name &8| &7$helpText &8[&c$page&8/&7$maxPage&8]")
    public static MessageProvider COMMAND_HELP_HEADER_FORMAT_END;
    @Message(id = "command.help.argument.format.required", content = "&8[&7$type&8: &c$name&8]")
    public static MessageProvider COMMAND_HELP_ARGUMENT_FORMAT_REQUIRED;
    @Message(id = "command.help.argument.format.optional", content = "&8<&7$type&8: &c$name&8>")
    public static MessageProvider COMMAND_HELP_ARGUMENT_FORMAT_OPTIONAL;
    @Message(id = "command.help.command.format.noargs", content = "&7$prefix &c$name &8- &7$description")
    public static MessageProvider COMMAND_HELP_COMMAND_FORMAT_NOARGS;
    @Message(id = "command.help.command.format.withargs", content = "&7$prefix &c$name $arguments &8- &7$description")
    public static MessageProvider COMMAND_HELP_COMMAND_FORMAT_WITHARGS;
    
    // Debug

    @Message(id = "command.debug.pdc.data.empty.block", content = "$#plugin.prefix Block at &c$x $y $z &7in &c$world&7 doesn't have any data.")
    public static MessageProvider COMMAND_DEBUG_PDC_DATA_EMPTY_BLOCK;
    @Message(id = "command.debug.pdc.data.empty.entity", content = "$#plugin.prefix Entity at &c$x $y $z &7in &c$world&7 doesn't have any data.")
    public static MessageProvider COMMAND_DEBUG_PDC_DATA_EMPTY_ENTITY;
    @Message(id = "command.debug.pdc.data.format.block", content = {
        "&8| $#plugin.name",
        "&8|",
        "&8| &7Data of block at &c$x $y $z &7in &c$world&7:",
        "&a$data"
    })
    public static MessageProvider COMMAND_DEBUG_PDC_DATA_FORMAT_BLOCK;
    @Message(id = "command.debug.pdc.data.format.entity", content = {
        "&8| $#plugin.name",
        "&8|",
        "&8| &7Data of entity at &c$x $y $z &7in &c$world&7:",
        "&a$data"
    })
    public static MessageProvider COMMAND_DEBUG_PDC_DATA_FORMAT_ENTITY;
    
    // Container

    @Message(id = "command.container.info.no-container.block", content = "$#plugin.prefix Block at &c$x $y $z &7in &c$world&7 is not an JustLootIt container!")
    public static MessageProvider COMMAND_CONTAINER_INFO_NO_CONTAINER_BLOCK;
    @Message(id = "command.container.info.no-container.entity", content = "$#plugin.prefix Entity at &c$x $y $z &7in &c$world&7 is not an JustLootIt container!")
    public static MessageProvider COMMAND_CONTAINER_INFO_NO_CONTAINER_ENTITY;
    @Message(id = "command.container.info.container.any.block", content = {
        "&8| $#plugin.name",
        "&8|",
        "&8| &7Block at &c$x $y $z &7in &c$world &7with Container (&c$id&7):",
        "&8|",
        "&8| &7Type: &c$type",
        "&8| &7Refresh Group: &c$refreshGroup",
    })
    public static MessageProvider COMMAND_CONTAINER_INFO_CONTAINER_ANY_BLOCK;
    @Message(id = "command.container.info.container.any.entity", content = {
        "&8| $#plugin.name",
        "&8|",
        "&8| &7Entity at &c$x $y $z &7in &c$world &7with Container (&c$id&7):",
        "&8|",
        "&8| &7Type: &c$type",
        "&8| &7Refresh Group: &c$refreshGroup",
    })
    public static MessageProvider COMMAND_CONTAINER_INFO_CONTAINER_ANY_ENTITY;
    @Message(id = "command.container.info.container.vanilla", content = {
        "&8|",
        "&8| &7Seed: &c$seed",
        "&8| &7Loot Table: &c$lootTable",
    })
    public static MessageProvider COMMAND_CONTAINER_INFO_CONTAINER_VANILLA;
    @Message(id = "command.container.info.container.frame", content = {
        "&8|",
        "&8| &7Item: &f$itemName&r &8(&7Hover for more info&8)"
    })
    public static MessageProvider COMMAND_CONTAINER_INFO_CONTAINER_FRAME;
    
    // Config

    @Message(id = "command.config.save.single", content = "$#plugin.prefix Saving config '&c$config&7'...")
    public static MessageProvider COMMAND_CONFIG_SAVE_SINGLE;
    @Message(id = "command.config.save.all.start", content = "$#plugin.prefix Saving configs...")
    public static MessageProvider COMMAND_CONFIG_SAVE_ALL_START;
    @Message(id = "command.config.save.all.end", content = "$#plugin.prefix Tried to save &c$total &7configs: &c$success &7successful, &c$skipped &7skipped, &c$failed &7failed.")
    public static MessageProvider COMMAND_CONFIG_SAVE_ALL_END;
    @Message(id = "command.config.save.result.skipped", content = "$#plugin.prefix Skipped saving config '&c$config&7'.")
    public static MessageProvider COMMAND_CONFIG_SAVE_RESULT_SKIPPED;
    @Message(id = "command.config.save.result.success", content = "$#plugin.prefix Successfully saved config '&c$config&7'.")
    public static MessageProvider COMMAND_CONFIG_SAVE_RESULT_SUCCESS;
    @Message(id = "command.config.save.result.failed", content = "$#plugin.prefix Failed to save config '&c$config&7', check the Server console for more info!")
    public static MessageProvider COMMAND_CONFIG_SAVE_RESULT_FAILED;

    @Message(id = "command.config.reload.single", content = "$#plugin.prefix Reloading config '&c$config&7'...")
    public static MessageProvider COMMAND_CONFIG_RELOAD_SINGLE;
    @Message(id = "command.config.reload.all.start", content = "$#plugin.prefix Reloadings configs...")
    public static MessageProvider COMMAND_CONFIG_RELOAD_ALL_START;
    @Message(id = "command.config.reload.all.end", content = "$#plugin.prefix Tried to reload &c$total &7configs: &c$success &7successful, &c$skipped &7skipped, &c$failed &7failed.")
    public static MessageProvider COMMAND_CONFIG_RELOAD_ALL_END;
    @Message(id = "command.config.reload.result.skipped", content = "$#plugin.prefix Skipped reloading config '&c$config&7'.")
    public static MessageProvider COMMAND_CONFIG_RELOAD_RESULT_SKIPPED;
    @Message(id = "command.config.reload.result.success", content = "$#plugin.prefix Successfully reloaded config '&c$config&7'.")
    public static MessageProvider COMMAND_CONFIG_RELOAD_RESULT_SUCCESS;
    @Message(id = "command.config.reload.result.failed", content = "$#plugin.prefix Failed to reload config '&c$config&7', check the Server console for more info!")
    public static MessageProvider COMMAND_CONFIG_RELOAD_RESULT_FAILED;

}
