package me.lauriichan.spigot.justlootit.command;

import java.util.List;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.CommandManager;
import me.lauriichan.laylib.command.Node;
import me.lauriichan.laylib.command.NodeAction;
import me.lauriichan.laylib.command.NodeArgument;
import me.lauriichan.laylib.command.NodeCommand;
import me.lauriichan.laylib.command.annotation.Action;
import me.lauriichan.laylib.command.annotation.Argument;
import me.lauriichan.laylib.command.annotation.Command;
import me.lauriichan.laylib.command.util.Triple;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.laylib.reflection.ClassUtil;

@Command(name = "help", aliases = {
    "?"
}, description = "command.description.justlootit.help")
public class HelpCommand {

    /*
     * Help command
     */

    @Action("")
    public void help(final CommandManager commandManager, final Actor<?> actor, @Argument(name = "command") final String command) {
        final Triple<NodeCommand, Node, String> triple = commandManager.findNode(command);
        if (triple == null) {
            actor.sendTranslatedMessage("command.help.command.none", Key.of("command", command));
            return;
        }
        final Node node = triple.getB();
        final NodeAction action = node.getAction();
        if (action == null) {
            if (!node.hasChildren()) {
                actor.sendTranslatedMessage("command.help.command.empty", Key.of("command", command),
                    Key.of("description", "$#" + triple.getA().getDescription()));
                return;
            }
            actor.sendTranslatedMessage("command.help.command.tree", Key.of("command", triple.getC()),
                Key.of("description", "$#" + triple.getA().getDescription()), Key.of("tree", generateTree(actor, node.getNames())));
            return;
        }
        final StringBuilder argumentBuilder = new StringBuilder(actor.getTranslatedMessageAsString("command.help.argument.format.header"));
        final List<NodeArgument> argumentList = action.getArguments();
        boolean found = false;
        for (int index = 0; index < argumentList.size(); index++) {
            final NodeArgument argument = argumentList.get(index);
            if (argument.isProvided()) {
                continue;
            }
            found = true;
            argumentBuilder.append('\n');
            final String type = ClassUtil.getClassName(argument.getArgumentType());
            if (argument.isOptional()) {
                argumentBuilder.append(actor.getTranslatedMessageAsString("command.help.argument.format.optional",
                    Key.of("name", argument.getName()), Key.of("type", type)));
                continue;
            }
            argumentBuilder.append(actor.getTranslatedMessageAsString("command.help.argument.format.required",
                Key.of("name", argument.getName()), Key.of("type", type)));
        }
        final String arguments = found ? argumentBuilder.toString()
            : actor.getTranslatedMessageAsString("command.help.argument.no-arguments");
        if (node.hasChildren()) {
            actor.sendTranslatedMessage("command.help.command.tree-executable", Key.of("command", triple.getC()),
                Key.of("description", "$#" + action.getDescription()), Key.of("arguments", arguments),
                Key.of("tree", generateTree(actor, node.getNames())));
            return;
        }
        actor.sendTranslatedMessage("command.help.command.executable", Key.of("command", triple.getC()),
            Key.of("description", "$#" + action.getDescription()), Key.of("arguments", arguments));
    }

    private String generateTree(final Actor<?> actor, final String[] names) {
        final StringBuilder builder = new StringBuilder();
        for (int index = 0; index < names.length; index++) {
            builder.append(actor.getTranslatedMessageAsString("command.help.tree.format", Key.of("name", names[index])));
            if (index + 1 != names.length) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }

}
