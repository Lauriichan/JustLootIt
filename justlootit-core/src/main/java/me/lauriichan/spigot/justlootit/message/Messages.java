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
    public static MessageProvider COMMAND_SYSTEM_ACTOR_NOT$SUPPORTED;

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
    @Message(id = "command.help.command.format.withargs", content = "&8$prefix &c$name $arguments &8- &7$description")
    public static MessageProvider COMMAND_HELP_COMMAND_FORMAT_WITHARGS;

}
