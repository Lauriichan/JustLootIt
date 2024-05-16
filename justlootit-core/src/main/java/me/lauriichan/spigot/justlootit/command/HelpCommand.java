package me.lauriichan.spigot.justlootit.command;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
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
import me.lauriichan.laylib.command.annotation.Description;
import me.lauriichan.laylib.command.annotation.Param;
import me.lauriichan.laylib.command.annotation.Permission;
import me.lauriichan.laylib.command.util.Triple;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.message.component.ComponentBuilder;
import me.lauriichan.spigot.justlootit.JustLootItPermission;
import me.lauriichan.spigot.justlootit.message.Messages;

@Extension
@Command(name = "help", aliases = "?")
@Permission(JustLootItPermission.COMMAND_HELP)
public class HelpCommand implements ICommandExtension {

    private static final Node[] EMPTY_NODES = new Node[0];

    private static final int HELP_PAGE_SIZE = 4;

    private static class HelpNodeTree {

        static record HelpNode(String path, Node node) {

            public String fullPath() {
                if (path.isEmpty()) {
                    return node.getName();
                }
                return path + ' ' + node.getName();
            }

            public boolean hasPermittedAction(Actor<?> actor) {
                NodeAction action = node.getAction();
                if (action == null) {
                    return false;
                }
                return action.getPermission() == null || actor.hasPermission(action.getPermission());
            }

            public Node[] getChildren(Actor<?> actor) {
                if (!this.node.hasChildren()) {
                    return EMPTY_NODES;
                }
                ObjectArrayList<Node> children = new ObjectArrayList<>();
                for (String name : this.node.getNames()) {
                    Node node = this.node.getNode(name);
                    if (!isSomethingPermitted(actor, node)) {
                        continue;
                    }
                    children.add(node);
                }
                if (children.isEmpty()) {
                    return EMPTY_NODES;
                }
                return children.toArray(Node[]::new);
            }

        }

        private final ObjectArrayList<HelpNode> paths = new ObjectArrayList<>();

        public HelpNodeTree() {}

        public HelpNodeTree(Node parent, Actor<?> actor) {
            ObjectArrayFIFOQueue<HelpNode> queue = new ObjectArrayFIFOQueue<>();
            queue.enqueue(new HelpNode("", parent));
            while (!queue.isEmpty()) {
                HelpNode current = queue.dequeue();
                if (current.hasPermittedAction(actor)) {
                    paths.add(current);
                }
                Node[] nodes = current.getChildren(actor);
                if (nodes.length == 0) {
                    continue;
                }
                String path = current.fullPath();
                for (Node node : nodes) {
                    queue.enqueue(new HelpNode(path, node));
                }
            }
            paths.sort((n1, n2) -> n1.fullPath().compareTo(n2.fullPath()));
        }

        public int amount() {
            return paths.size();
        }

        public HelpNode path(int index) {
            return paths.get(index);
        }

        public void merge(HelpNodeTree tree) {
            paths.addAll(tree.paths);
        }

    }

    @Action("")
    @Description("$#command.description.justlootit.help.all")
    public void helpOverview(CommandManager commandManager, Actor<?> actor, @Argument(name = "page", optional = true, index = 1, params = {
        @Param(name = "minimum", type = Param.TYPE_INT, intValue = 1)
    }) int page) {
        List<NodeCommand> commands = commandManager.getCommands();
        HelpNodeTree tree = new HelpNodeTree();
        for (NodeCommand command : commands) {
            if (command.isRestricted() && !actor.hasPermission(command.getPermission())) {
                continue;
            }
            HelpNodeTree current = new HelpNodeTree(command.getNode(), actor);
            if (current.amount() == 0) {
                continue;
            }
            tree.merge(current);
        }
        if (tree.amount() == 0) {
            actor.sendTranslatedMessage(Messages.COMMAND_HELP_NONE);
            return;
        }
        showHelpTree(commandManager, actor, tree, page, commandManager.getPrefix(),
            actor.getTranslatedMessageAsString(Messages.COMMAND_HELP_OVERVIEW_HEADER), "{0} {2}");
    }

    @Action("command")
    @Description("$#command.description.justlootit.help.command")
    public void help(CommandManager commandManager, Actor<?> actor, @Argument(name = "command", optional = true, index = 1) String command,
        @Argument(name = "page", optional = true, index = 2, params = {
            @Param(name = "minimum", type = Param.TYPE_INT, intValue = 1)
        }) int page) {
        if (command == null || command.isBlank()) {
            helpOverview(commandManager, actor, page);
            return;
        }
        Triple<NodeCommand, Node, String> triple = commandManager.findNode(command = command.trim().replaceAll(" {2,}", " "));
        if (triple == null || (triple.getA().isRestricted() && !actor.hasPermission(triple.getA().getPermission()))) {
            actor.sendTranslatedMessage(Messages.COMMAND_HELP_UNKNOWN, Key.of("command", command));
            return;
        }
        if (!triple.getC().substring(commandManager.getPrefix().length()).equals(command)) {
            actor.sendTranslatedMessage(Messages.COMMAND_HELP_UNKNOWN, Key.of("command", command));
            return;
        }
        showHelpTree(commandManager, actor, new HelpNodeTree(triple.getB(), actor), page,
            getPrefix(commandManager.getPrefix(), triple.getB()), command, "{0} command '{1}' {2}");
    }

    private void showHelpTree(CommandManager commandManager, Actor<?> actor, HelpNodeTree tree, int page, String prefix, String helpText,
        String arrowCommandFormat) {
        prefix = prefix.trim();
        int total = tree.amount();
        int maxPage = Math.floorDiv(total, HELP_PAGE_SIZE) + (total % HELP_PAGE_SIZE != 0 ? 1 : 0);
        page = Math.min(Math.max(page, 1), maxPage);

        actor.sendTranslatedMessage(Messages.COMMAND_HELP_HEADER_FORMAT_START, Key.of("helpText", helpText), Key.of("page", page),
            Key.of("maxPage", maxPage));
        actor.sendMessage(""); // Add one space
        int maxIndex = Math.min((page - 1) * HELP_PAGE_SIZE + HELP_PAGE_SIZE, total);
        for (int index = (page - 1) * HELP_PAGE_SIZE; index < maxIndex; index++) {
            HelpNodeTree.HelpNode helpNode = tree.path(index);
            NodeAction action = helpNode.node().getAction();
            List<NodeArgument> argumentList = action.getArguments().stream().filter(arg -> !arg.isProvided()).toList();
            if (argumentList.isEmpty()) {
                actor.sendTranslatedMessage(Messages.COMMAND_HELP_COMMAND_FORMAT_NOARGS, Key.of("prefix", prefix),
                    Key.of("name", helpNode.fullPath()), Key.of("description", action.getDescription()));
                continue;
            }
            StringBuilder arguments = new StringBuilder();
            for (int i = 0; i < argumentList.size(); i++) {
                if (i != 0) {
                    arguments.append(' ');
                }
                NodeArgument argument = argumentList.get(i);
                arguments.append(actor.getTranslatedMessageAsString(
                    argument.isOptional() ? Messages.COMMAND_HELP_ARGUMENT_FORMAT_OPTIONAL : Messages.COMMAND_HELP_ARGUMENT_FORMAT_REQUIRED,
                    Key.of("type", ClassUtil.getClassName(argument.getArgumentType())), Key.of("name", argument.getName())));
            }
            actor.sendTranslatedMessage(Messages.COMMAND_HELP_COMMAND_FORMAT_WITHARGS, Key.of("prefix", prefix),
                Key.of("name", helpNode.fullPath()), Key.of("arguments", arguments.toString()),
                Key.of("description", action.getDescription()));
        }
        actor.sendMessage(""); // Add one space
        if (actor.getId() != Actor.IMPL_ID) {
            ComponentBuilder<?, ?> component = ComponentBuilder.create();
            if (page != 1) {
                component.appendContent(Messages.COMMAND_SYSTEM_ARROW_LEFT, actor.getLanguage())
                    .clickRun(arrowCommandFormat, commandManager.getPrefix() + "help", helpText, page - 1)
                    .hoverText(Messages.COMMAND_SYSTEM_PAGE_PREVIOUS, actor.getLanguage()).finish();
            }
            if (page != maxPage) {
                if (page != 1) {
                    component.appendContent(Messages.COMMAND_SYSTEM_ARROW_SEPERATOR, actor.getLanguage()).finish();
                }
                component.appendContent(Messages.COMMAND_SYSTEM_ARROW_RIGHT, actor.getLanguage())
                    .clickRun(arrowCommandFormat, commandManager.getPrefix() + "help", helpText, page + 1)
                    .hoverText(Messages.COMMAND_SYSTEM_PAGE_NEXT, actor.getLanguage()).finish();
            }
            if (!component.isEmpty()) {
                component.send(actor);
            }
        }
        actor.sendTranslatedMessage(Messages.COMMAND_HELP_HEADER_FORMAT_END, Key.of("helpText", helpText), Key.of("page", page),
            Key.of("maxPage", maxPage));
    }

    private String getPrefix(String prefix, Node command) {
        Node root = command.getRoot();
        if (root == command) {
            return prefix;
        }
        StringBuilder builder = new StringBuilder(prefix).append(root.getName());
        for (Node parent = command.getParent(); parent != root; parent = parent.getParent()) {
            builder.append(" ").append(parent.getName());
        }
        return builder.toString();
    }

    private static boolean isSomethingPermitted(Actor<?> actor, Node node) {
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
}