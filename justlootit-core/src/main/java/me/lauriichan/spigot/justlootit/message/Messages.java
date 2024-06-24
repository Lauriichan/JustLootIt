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

    @Message(id = "plugin.name", content = "&#[EA288C-C66CF7]JustLootIt")
    public static MessageProvider NAME;
    @Message(id = "plugin.prefix", content = "$#plugin.name &8|&7")
    public static MessageProvider PREFIX;
    
    /*
     * Input
     */
    @Message(id = "input.manual.cancel", content = "$#plugin.prefix &7Successfully cancelled input.")
    public static MessageProvider INPUT_MANUAL_CANCEL;
    
    @Message(id = "input.betterinputs.cancelled", content = "$#plugin.prefix &7Input inquiry cancelled: &c$reason")
    public static MessageProvider INPUT_BETTERINPUTS_CANCELLED;
    @Message(id = "input.betterinputs.failed", content = "$#plugin.prefix &7Input inquiry failed: &c$message")
    public static MessageProvider INPUT_BETTERINPUTS_FAILED;
    @Message(id = "input.betterinputs.prompt", content = "$#plugin.prefix &7$prompt")
    public static MessageProvider INPUT_BETTERINPUTS_PROMPT;

    @Message(id = "input.simple.failed", content = "$#plugin.prefix &7Input inquiry failed: &c$message")
    public static MessageProvider INPUT_SIMPLE_FAILED;
    @Message(id = "input.simple.prompt", content = "$#plugin.prefix &7$prompt")
    public static MessageProvider INPUT_SIMPLE_PROMPT;
    
    // Retries
    @Message(id = "input.retry.loottable.seed", content = "$#plugin.prefix Invalid seed, please try again.")
    public static MessageProvider INPUT_RETRY_LOOTTABLE_SEED;
    @Message(id = "input.retry.loottable.key", content = "$#plugin.prefix Invalid loot table, please try again.")
    public static MessageProvider INPUT_RETRY_LOOTTABLE_KEY;
    @Message(id = "input.retry.boolean", content = {
        "$#plugin.prefix Invalid boolean, please try again.",
        "&aTrue &7values: yes, y, true, on",
        "&cFalse &7values: no, n, false, off"
    })
    public static MessageProvider INPUT_RETRY_BOOLEAN;

    // Prompts
    @Message(id = "input.prompt.loottable.seed", content = "&7Enter loot table seed")
    public static MessageProvider INPUT_PROMPT_LOOTTABLE_SEED;
    @Message(id = "input.prompt.loottable.key", content = "&7Enter loot table key")
    public static MessageProvider INPUT_PROMPT_LOOTTABLE_KEY;
    
    @Message(id = "input.prompt.convert.do-lootin", content = "&7Do you want to convert Lootin containers?")
    public static MessageProvider INPUT_PROMPT_CONVERT_DO_LOOTIN;
    @Message(id = "input.prompt.convert.disallow-lootin-static", content = {
        "&7Do you want to disable the conversion of static Lootin containers (same loot every time)?",
        "&7Recommended: no"
    })
    public static MessageProvider INPUT_PROMPT_CONVERT_LOOTIN_STATIC;
    @Message(id = "input.prompt.convert.do-vanilla", content = "&7Do you want to convert Vanilla containers?")
    public static MessageProvider INPUT_PROMPT_CONVERT_DO_VANILLA;
    @Message(id = "input.prompt.convert.vanilla-item-frames", content = "&7Do you want to convert Vanilla item frames?")
    public static MessageProvider INPUT_PROMPT_CONVERT_VANILLA_ITEM_FRAMES;
    @Message(id = "input.prompt.convert.vanilla-eltry-frames-only", content = {
        "&7Do you want to only allow Vanilla elytra item frames?",
        "&7Recommended: yes",
        "&4WARNING: &c*NOT* using this in a world with player created item frames might cause them to be recognized as Loot containers!"
    })
    public static MessageProvider INPUT_PROMPT_CONVERT_VANILLA_ELYTRA_FRAMES_ONLY;
    @Message(id = "input.prompt.convert.allow-vanilla-static", content = {
        "&7Do you want to allow the conversion Vanilla static containers?",
        "&7Recommended: no",
        "&4WARNING: &cDoing this in a world with player created blocks might cause them to be recognized as Loot containers!"
    })
    public static MessageProvider INPUT_PROMPT_CONVERT_VANILLA_STATIC_CONTAINERS;
    
    /*
     * Container
     */
    @Message(id = "container.compatibility.not-active", content = "$#plugin.prefix This loot container can not be accessed because the plugin '&c$plugin&7' is not active to fill it.")
    public static MessageProvider CONTAINER_COMPATIBILITY_NOT_ACTIVE;
    @Message(id = "container.compatibility.fill-not-available", content = "$#plugin.prefix This loot container can not be accessed because the data provided by plugin '&c$plugin&7' related to the container is probably no longer available.")
    public static MessageProvider CONTAINER_COMPATIBILITY_FILL_NOT_AVAILABLE;
    @Message(id = "container.compatibility.fill-failed", content = "$#plugin.prefix This loot container can not be accessed because the compatibility for plugin '&c$plugin&7' failed to fill it.")
    public static MessageProvider CONTAINER_COMPATIBILITY_FILL_FAILED;
    
    @Message(id = "container.access.not-repeatable", content = "&7Can &cnever &7be accessed again.")
    public static MessageProvider CONTAINER_ACCESS_NOT_REPEATABLE;
    @Message(id = "container.access.not-accessible", content = "&7Not accessible for $time&7.")
    public static MessageProvider CONTAINER_ACCESS_NOT_ACCESSIBLE;
    @Message(id = "container.access.wait-for-access", content = "$#plugin.prefix Please wait a moment for JLI to process your last container access.")
    public static MessageProvider CONTAINER_ACCESS_WAIT_FOR_ACCESS;

    @Message(id = "container.break.unpermitted.block", content = "$#plugin.prefix This entity is a loot container can not be removed.")
    public static MessageProvider CONTAINER_BREAK_UNPERMITTED_BLOCK;
    @Message(id = "container.break.unpermitted.entity", content = "$#plugin.prefix This block is a loot container can not be removed.")
    public static MessageProvider CONTAINER_BREAK_UNPERMITTED_ENTITY;

    @Message(id = "container.break.permitted.block", content = "$#plugin.prefix This entity is a loot container, you have to sneak in order to remove it.")
    public static MessageProvider CONTAINER_BREAK_PERMITTED_BLOCK;
    @Message(id = "container.break.permitted.entity", content = "$#plugin.prefix This block is a loot container, you have to sneak in order to remove it.")
    public static MessageProvider CONTAINER_BREAK_PERMITTED_ENTITY;
    
    @Message(id = "container.break.confirmation.block", content = "$#plugin.prefix Please break the container block again in the next &c2 minutes &7in order to remove it.")
    public static MessageProvider CONTAINER_BREAK_CONFIRMATION_BLOCK;
    @Message(id = "container.break.confirmation.entity", content = "$#plugin.prefix Please hit the container entity again in the next &c2 minutes &7in order to remove it.")
    public static MessageProvider CONTAINER_BREAK_CONFIRMATION_ENTITY;
    
    @Message(id = "container.break.no-container", content = "$#plugin.prefix Container &8(&c$id&8) &7is already deleted.")
    public static MessageProvider CONTAINER_BREAK_NO_CONTAINER;
    
    @Message(id = "container.break.double-chest", content = "$#plugin.prefix Successfully removed chest half.")
    public static MessageProvider CONTAINER_BREAK_DOUBLE_CHEST;
    
    @Message(id = "container.break.removed.block", content = "$#plugin.prefix Succesfully removed container &8(&c$id&8) &7from block.")
    public static MessageProvider CONTAINER_BREAK_REMOVED_BLOCK;
    @Message(id = "container.break.removed.entity", content = "$#plugin.prefix Succesfully removed container &8(&c$id&8) &7from entity.")
    public static MessageProvider CONTAINER_BREAK_REMOVED_ENTITY;
    
    @Message(id = "container.time.unit.millisecond", content = "$value millisecond(s)")
    public static MessageProvider CONTAINER_TIME_UNIT_MILLISECOND;
    @Message(id = "container.time.unit.second", content = "$value second(s)")
    public static MessageProvider CONTAINER_TIME_UNIT_SECOND;
    @Message(id = "container.time.unit.minute", content = "$value minute(s)")
    public static MessageProvider CONTAINER_TIME_UNIT_MINUTE;
    @Message(id = "container.time.unit.hour", content = "$value hour(s)")
    public static MessageProvider CONTAINER_TIME_UNIT_HOUR;
    @Message(id = "container.time.unit.day", content = "$value day(s)")
    public static MessageProvider CONTAINER_TIME_UNIT_DAY;

    @Message(id = "container.time.format.seconds", content = "&c$seconds &7and &c$milliseconds")
    public static MessageProvider CONTAINER_TIME_FORMAT_SECONDS;
    @Message(id = "container.time.format.minutes", content = "&c$minutes &7and &c$seconds")
    public static MessageProvider CONTAINER_TIME_FORMAT_MINUTES;
    @Message(id = "container.time.format.hours", content = "&c$hours &7and &c$minutes")
    public static MessageProvider CONTAINER_TIME_FORMAT_HOURS;
    @Message(id = "container.time.format.days", content = "&c$days &7and &c$hours")
    public static MessageProvider CONTAINER_TIME_FORMAT_DAYS;

    /*
     * Commands
     */

    // System (general)

    @Message(id = "command.system.actor.not-supported", content = "$#plugin.prefix This can only be excuted by actors of type &c$actorType&7.")
    public static MessageProvider COMMAND_SYSTEM_ACTOR_NOT_SUPPORTED;
    @Message(id = "command.system.actor.coords-required.player", content = "$#plugin.prefix Please specify all x, y and z coordinates or look at a block.")
    public static MessageProvider COMMAND_SYSTEM_ACTOR_COORDS_REQUIRED_PLAYER;
    @Message(id = "command.system.actor.coords-required.non-player", content = "$#plugin.prefix Please specify all x, y and z coordinates.")
    public static MessageProvider COMMAND_SYSTEM_ACTOR_COORDS_REQUIRED_NON_PLAYER;
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
    
    // Group
    
    @Message(id = "command.group.all.unsupported", content = "$#plugin.prefix The time unit '&c$unit&7' is not supported by JustLootIt!")
    public static MessageProvider COMMAND_GROUP_ALL_UNSUPPORTED;
    @Message(id = "command.group.all.never-interval", content = "never")
    public static MessageProvider COMMAND_GROUP_ALL_NEVER;

    @Message(id = "command.group.create.success", content = "$#plugin.prefix Successfully created the refresh group '&c$group&7' with an interval of &c$time&7.")
    public static MessageProvider COMMAND_GROUP_CREATE_SUCCESS;
    @Message(id = "command.group.create.already-exists", content = "$#plugin.prefix There is already a refresh group named '&c$group&7'!")
    public static MessageProvider COMMAND_GROUP_CREATE_ALREADY_EXISTS;

    @Message(id = "command.group.set", content = "$#plugin.prefix Successfully set the interval of refresh group '&c$group&7' to &c$time&7.")
    public static MessageProvider COMMAND_GROUP_SET;

    @Message(id = "command.group.info", content = "$#plugin.prefix Interval of refresh group '&c$group&7' is currently set to &c$time&7.")
    public static MessageProvider COMMAND_GROUP_INFO;

    @Message(id = "command.group.delete", content = "$#plugin.prefix Successfully deleted refresh group '&c$group&7'.")
    public static MessageProvider COMMAND_GROUP_DELETE;

    @Message(id = "command.group.list.no-entries", content = "$#plugin.prefix There are no refresh groups available!")
    public static MessageProvider COMMAND_GROUP_LIST_NO_ENTRIES;
    @Message(id = "command.group.list.format.header", content = "$#plugin.name &8| &7Refresh groups &8[&c$page&8/&7$maxPage&8]")
    public static MessageProvider COMMAND_GROUP_LIST_FORMAT_HEADER;
    @Message(id = "command.group.list.format.entry.text", content = "&8- &c$group &8(&7$time&8)")
    public static MessageProvider COMMAND_GROUP_LIST_FORMAT_ENTRY_TEXT;
    @Message(id = "command.group.list.format.entry.hover", content = "&7Click to get edit command")
    public static MessageProvider COMMAND_GROUP_LIST_FORMAT_ENTRY_HOVER;
    
    // Container

    @Message(id = "command.container.all.no-container.block", content = "$#plugin.prefix Block at &c$x $y $z &7in &c$world&7 is not a JustLootIt container!")
    public static MessageProvider COMMAND_CONTAINER_ALL_NO_CONTAINER_BLOCK;
    @Message(id = "command.container.all.no-container.entity", content = "$#plugin.prefix Entity at &c$x $y $z &7in &c$world&7 is not a JustLootIt container!")
    public static MessageProvider COMMAND_CONTAINER_ALL_NO_CONTAINER_ENTITY;
    @Message(id = "command.container.all.no-container.id", content = "$#plugin.prefix Couldn't find JustLootIt container with id &c$id&7!")
    public static MessageProvider COMMAND_CONTAINER_ALL_NO_CONTAINER_ID;
    
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
    
    @Message(id = "command.container.create.already-container.entity", content = "$#plugin.prefix Entity at &c$x $y $z &7in &c$world &7is already linked to a JustLootIt Container.")
    public static MessageProvider COMMAND_CONTAINER_CREATE_ALREADY_CONTAINER_ENTITY;
    @Message(id = "command.container.create.already-container.block", content = "$#plugin.prefix Block at &c$x $y $z &7in &c$world &7is already linked to a JustLootIt Container.")
    public static MessageProvider COMMAND_CONTAINER_CREATE_ALREADY_CONTAINER_BLOCK;
    @Message(id = "command.container.create.unsupported.block", content = "$#plugin.prefix Block at &c$x $y $z &7in &c$world &7is not supported to be used as &c$type container&7!")
    public static MessageProvider COMMAND_CONTAINER_CREATE_UNSUPPORTED_BLOCK;
    @Message(id = "command.container.create.unsupported.entity", content = "$#plugin.prefix Entity at &c$x $y $z &7in &c$world &7is not supported to be used as &c$type container&7!")
    public static MessageProvider COMMAND_CONTAINER_CREATE_UNSUPPORTED_ENTITY;
    @Message(id = "command.container.create.changed.block", content = "$#plugin.prefix Block at &c$x $y $z &7in &c$world &7has changed, please try again!")
    public static MessageProvider COMMAND_CONTAINER_CREATE_CHANGED_BLOCK;
    @Message(id = "command.container.create.changed.entity", content = "$#plugin.prefix Entity at &c$x $y $z &7in &c$world &7has died, please try again!")
    public static MessageProvider COMMAND_CONTAINER_CREATE_CHANGED_ENTITY;
    @Message(id = "command.container.create.success.block", content = "$#plugin.prefix Successfully linked block at &c$x $y $z &7in &c$world &7to a &c$type container &8(&c$id&8)&7.")
    public static MessageProvider COMMAND_CONTAINER_CREATE_SUCCESS_BLOCK;
    @Message(id = "command.container.create.success.entity", content = "$#plugin.prefix Successfully linked entity at &c$x $y $z &7in &c$world &7to a &c$type container &8(&c$id&8)&7.")
    public static MessageProvider COMMAND_CONTAINER_CREATE_SUCCESS_ENTITY;
    @Message(id = "command.container.create.not-found.block", content = "$#plugin.prefix No block found at &c$x $y $z &7in &c$world&7.")
    public static MessageProvider COMMAND_CONTAINER_CREATE_NOT_FOUND_BLOCK;
    @Message(id = "command.container.create.not-found.entity", content = "$#plugin.prefix No valid entity around &c$x $y $z &7in &c$world&7.")
    public static MessageProvider COMMAND_CONTAINER_CREATE_NOT_FOUND_ENTITY;
    @Message(id = "command.container.create.not-creatable", content = "$#plugin.prefix The &c$type container &7can not be created manually.")
    public static MessageProvider COMMAND_CONTAINER_CREATE_NOT_CREATABLE;
    @Message(id = "command.container.create.frame.item-required", content = "$#plugin.prefix The item frame at &c$x $y $z &7in &c$world &7has to contain a valid item!")
    public static MessageProvider COMMAND_CONTAINER_CREATE_FRAME_ITEM_REQUIRED;
    
    @Message(id = "command.container.group.set.block", content = "$#plugin.prefix Successfully set the refresh group of the container linked to the block at &c$x $y $z &7in &c$world &7to &c$group&7.")
    public static MessageProvider COMMAND_CONTAINER_GROUP_SET_BLOCK;
    @Message(id = "command.container.group.set.entity", content = "$#plugin.prefix Successfully set the refresh group of the container linked to the entity at &c$x $y $z &7in &c$world &7to &c$group&7.")
    public static MessageProvider COMMAND_CONTAINER_GROUP_SET_ENTITY;
    @Message(id = "command.container.group.removed.block", content = "$#plugin.prefix Successfully removed the refresh group from the container linked to the block at &c$x $y $z &7in &c$world&7.")
    public static MessageProvider COMMAND_CONTAINER_GROUP_REMOVED_BLOCK;
    @Message(id = "command.container.group.removed.entity", content = "$#plugin.prefix Successfully removed the refresh group from the container linked to the entity at &c$x $y $z &7in &c$world&7.")
    public static MessageProvider COMMAND_CONTAINER_GROUP_REMOVED_ENTITY;
    
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
    
    // Convert
    
    @Message(id = "command.convert.proccess-on-going", content = "$#plugin.prefix &cThere is already a conversion process setup on-going!")
    public static MessageProvider COMMAND_CONVERT_PROCESS_ONGOING;
    @Message(id = "command.convert.process-done", content = "$#plugin.prefix &7Successfully set up conversion process, restarting server in 5 seconds (might require manual start)")
    public static MessageProvider COMMAND_CONVERT_PROCESS_DONE;
    
}
