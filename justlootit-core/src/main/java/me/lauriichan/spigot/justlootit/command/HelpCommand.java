package me.lauriichan.spigot.justlootit.command;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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

    private static final String[] EMPTY_ARRAY = new String[0];

    @Action("")
    public void help(CommandManager commandManager, Actor<?> actor, @Argument(name = "command", optional = true) String command) {
        if (command == null || command.isBlank()) {
            List<NodeCommand> nodeCommands = commandManager.getCommands();
            StringBuilder builder = new StringBuilder();
            NodeCommand nodeCommand;
            int count = 0;
            for (int index = 0; index < nodeCommands.size(); index++) {
                nodeCommand = nodeCommands.get(index);
                if (nodeCommand.isRestricted() && !actor.hasPermission(nodeCommand.getPermission())) {
                    continue;
                }
                if (count++ != 0) {
                    builder.append('\n');
                }
                builder.append(actor.getTranslatedMessageAsString("command.help.tree.format.overview",
                    Key.of("name", nodeCommand.getName()), Key.of("description", nodeCommand.getDescription())));
            }
            if (count == 0) {
                actor.sendTranslatedMessage("command.help.command.none");
                return;
            }
            actor.sendTranslatedMessage("command.help.command.overview", Key.of("tree", builder.toString()));
            return;
        }
        Triple<NodeCommand, Node, String> triple = commandManager.findNode(command);
        if (triple == null || (triple.getA().isRestricted() && !actor.hasPermission(triple.getA().getPermission()))) {
            actor.sendTranslatedMessage("command.help.command.unknown", Key.of("command", command));
            return;
        }
        Node node = triple.getB();
        if (!isSomethingPermitted(actor, node)) {
            actor.sendTranslatedMessage("command.help.command.unknown", Key.of("command", command));
            return;
        }
        NodeAction action = node.getAction();
        if (action != null && (!action.isRestricted() || actor.hasPermission(action.getPermission()))) {
            StringBuilder argumentBuilder = new StringBuilder(actor.getTranslatedMessageAsString("command.help.argument.format.header"));
            List<NodeArgument> argumentList = action.getArguments();
            boolean found = false;
            for (int index = 0; index < argumentList.size(); index++) {
                NodeArgument argument = argumentList.get(index);
                if (argument.isProvided()) {
                    continue;
                }
                found = true;
                argumentBuilder.append('\n');
                String type = ClassUtil.getClassName(argument.getArgumentType());
                if (argument.isOptional()) {
                    argumentBuilder.append(actor.getTranslatedMessageAsString("command.help.argument.format.optional",
                        Key.of("name", argument.getName()), Key.of("type", type)));
                    continue;
                }
                argumentBuilder.append(actor.getTranslatedMessageAsString("command.help.argument.format.required",
                    Key.of("name", argument.getName()), Key.of("type", type)));
            }
            String arguments = found ? argumentBuilder.toString()
                : actor.getTranslatedMessageAsString("command.help.argument.no-arguments");
            if (node.hasChildren()) {
                String[] children = filterChildren(actor, node);
                if (children.length != 0) {
                    actor.sendTranslatedMessage("command.help.command.tree-executable", Key.of("command", triple.getC()),
                        Key.of("description", action.getDescription()), Key.of("arguments", arguments),
                        Key.of("tree", generateTree(actor, children)));
                    return;
                }
            }
            actor.sendTranslatedMessage("command.help.command.executable", Key.of("command", triple.getC()),
                Key.of("description", action.getDescription()), Key.of("arguments", arguments));
            return;
        }
        if (node.hasChildren()) {
            String[] children = filterChildren(actor, node);
            if (children.length != 0) {
                actor.sendTranslatedMessage("command.help.command.tree", Key.of("command", triple.getC()),
                    Key.of("description", triple.getA().getDescription()), Key.of("tree", children));
                return;
            }
        }
        actor.sendTranslatedMessage("command.help.command.empty", Key.of("command", command),
            Key.of("description", triple.getA().getDescription()));
        return;
    }

    private String[] filterChildren(Actor<?> actor, Node node) {
        if (!node.hasChildren()) {
            return EMPTY_ARRAY;
        }
        ObjectArrayList<String> children = new ObjectArrayList<>();
        for (String name : node.getNames()) {
            if (!isSomethingPermitted(actor, node.getNode(name))) {
                continue;
            }
            children.add(name);
        }
        if (children.isEmpty()) {
            return EMPTY_ARRAY;
        }
        return children.toArray(String[]::new);
    }

    private boolean isSomethingPermitted(Actor<?> actor, Node node) {
        NodeAction action = node.getAction();
        if (action != null) {
            if (!action.isRestricted() || actor.hasPermission(action.getPermission())) {
                return true;
            }
            if (!node.hasChildren()) {
                return false;
            }
        } else if (!node.hasChildren()) {
            return true;
        }
        for (String name : node.getNames()) {
            if (isSomethingPermitted(actor, node.getNode(name))) {
                return true;
            }
        }
        return false;
    }

    private String generateTree(Actor<?> actor, String[] names) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < names.length; index++) {
            builder.append(actor.getTranslatedMessageAsString("command.help.tree.format.normal", Key.of("name", names[index])));
            if (index + 1 != names.length) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }
}