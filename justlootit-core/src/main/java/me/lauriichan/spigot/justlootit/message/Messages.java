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

    // Help

    @Message(id = "command.help.command.none", content = "$#plugin.prefix There are no commands to list that you are permitted to see!")
    public static MessageProvider COMMAND_HELP_NONE;
    @Message(id = "command.help.command.unknown", content = "$#plugin.prefix The command '&c$command&7' doesn't exist!")
    public static MessageProvider COMMAND_HELP_UNKNOWN;
    @Message(id = "command.help.command.overview", content = {
        "&8=< $#plugin.name &8>-< &7Overview",
        " ",
        "$tree",
        " ",
        "&8=< $#plugin.name &8>-< &7Overview"
    })
    public static MessageProvider COMMAND_HELP_OVERVIEW;
    @Message(id = "command.help.command.empty", content = {
        "&8=< $#plugin.name &8>-< &7$command",
        " ",
        "&7$description",
        " ",
        "&8=< $#plugin.name &8>-< &7$command"
    })
    public static MessageProvider COMMAND_HELP_EMPTY;
    @Message(id = "command.help.command.tree", content = {
        "&8=< $#plugin.name &8>-< &7$command",
        " ",
        "&7$description",
        " ",
        "&7This command has following subcommands:",
        "$tree",
        " ",
        "&8=< $#plugin.name &8>-< &7$command"
    })
    public static MessageProvider COMMAND_HELP_TREE;
    @Message(id = "command.help.command.executable", content = {
        "&8=< $#plugin.name &8>-< &7$command",
        " ",
        "&7$description",
        " ",
        "&7$arguments",
        " ",
        "&7This command can be executed.",
        " ",
        "&8=< $#plugin.name &8>-< &7$command"
    })
    public static MessageProvider COMMAND_HELP_EXECUATABLE;
    @Message(id = "command.help.command.tree-executable", content = {
        "&8=< $#plugin.name &8>-< &7$command",
        " ",
        "&7$description",
        " ",
        "&7$arguments",
        " ",
        "&7This command can be executed and has following subcommands:",
        "$tree",
        " ",
        "&8=< $#plugin.name &8>-< &7$command"
    })
    public static MessageProvider COMMAND_HELP_TREE$EXECUTABLE;
    @Message(id = "command.help.argument.no-arguments", content = "There are no arguments for this command")
    public static MessageProvider COMMAND_HELP_ARGUMENT_NO$ARGUMENTS;
    @Message(id = "command.help.argument.format.header", content = "&7[required] <optional>")
    public static MessageProvider COMMAND_HELP_ARGUMENT_FORMAT_HEADER;
    @Message(id = "command.help.argument.format.required", content = "&8[&7$type&8] &7$name")
    public static MessageProvider COMMAND_HELP_ARGUMENT_FORMAT_REQUIRED;
    @Message(id = "command.help.argument.format.optional", content = "&8<&7$type&8> &7$name")
    public static MessageProvider COMMAND_HELP_ARGUMENT_FORMAT_OPTIONAL;
    @Message(id = "command.help.tree.format.normal", content = "&8- &7$name")
    public static MessageProvider COMMAND_HELP_TREE_FORMAT_NORMAL;
    @Message(id = "command.help.tree.format.overview", content = "&8/town &7$name &8- &7$description")
    public static MessageProvider COMMAND_HELP_TREE_FORMAT_OVERVIEW;

}
