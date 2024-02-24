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
import me.lauriichan.laylib.command.annotation.Param;
import me.lauriichan.laylib.command.util.Triple;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.laylib.localization.MessageProvider;
import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.minecraft.pluginbase.message.component.Component;
import me.lauriichan.minecraft.pluginbase.message.component.ComponentCompound;
import me.lauriichan.spigot.justlootit.message.Messages;

@Command(name = "help", aliases = {
    "?"
}, description = "command.description.justlootit.help")
public class HelpCommand {

    private static final Node[] EMPTY_NODES = new Node[0];

    private static final int HELP_PAGE_SIZE = 8;

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

    @Action("all")
    public void helpOverview(CommandManager commandManager, Actor<?> actor, @Argument(name = "page", optional = true, index = 1, params = {
        @Param(name = "min", type = Param.TYPE_INT, intValue = 1)
    }) int page) {
        List<NodeCommand> commands = commandManager.getCommands().stream().filter(cmd -> isSomethingPermitted(actor, cmd.getNode()))
            .toList();
        if (commands.isEmpty()) {
            actor.sendTranslatedMessage(Messages.COMMAND_HELP_NONE);
            return;
        }
        HelpNodeTree tree = new HelpNodeTree();
        for (NodeCommand command : commands) {
            tree.merge(new HelpNodeTree(command.getNode(), actor));
        }
        showHelpTree(commandManager, actor, tree, page, commandManager.getPrefix(), actor.getTranslatedMessageAsString(Messages.COMMAND_HELP_OVERVIEW_HEADER), "{0} all {2}");
    }

    @Action("")
    @Action("command")
    public void help(CommandManager commandManager, Actor<?> actor, @Argument(name = "command", optional = true, index = 1) String command,
        @Argument(name = "page", optional = true, index = 2, params = {
            @Param(name = "min", type = Param.TYPE_INT, intValue = 1)
        }) int page) {
        if (command == null || command.isBlank()) {
            helpOverview(commandManager, actor, page);
            return;
        }
        Triple<NodeCommand, Node, String> triple = commandManager.findNode(command);
        if (triple == null || (triple.getA().isRestricted() && !actor.hasPermission(triple.getA().getPermission()))) {
            actor.sendTranslatedMessage(Messages.COMMAND_HELP_UNKNOWN, Key.of("command", command));
            return;
        }
        Node commandNode = triple.getB();
        if (!isSomethingPermitted(actor, commandNode)) {
            actor.sendTranslatedMessage(Messages.COMMAND_HELP_UNKNOWN, Key.of("command", command));
            return;
        }
        showHelpTree(commandManager, actor, new HelpNodeTree(commandNode, actor), page, getPrefix(commandManager.getPrefix(), commandNode), command, "{0} command '{1}' {2}");
    }

    private void showHelpTree(CommandManager commandManager, Actor<?> actor, HelpNodeTree tree, int page, String prefix, String helpText,
        String arrowCommandFormat) {
        int maxPage = Math.floorDiv(tree.amount(), HELP_PAGE_SIZE) + (tree.amount() % HELP_PAGE_SIZE != 0 ? 1 : 0);
        page = Math.min(page, maxPage);

        ComponentCompound component = ComponentCompound.create();
        if (page != maxPage) {
            component.add(Component.of(Messages.COMMAND_SYSTEM_ARROW_LEFT, actor.getLanguage()).clickRun(arrowCommandFormat,
                commandManager.getPrefix(), helpText, page - 1).hoverText(Messages.COMMAND_SYSTEM_PAGE_NEXT, actor.getLanguage()));
        }
        if (page != 1) {
            if (page != maxPage) {
                component.add(Component.of(Messages.COMMAND_SYSTEM_ARROW_SEPERATOR, actor.getLanguage()));
            }
            component.add(Component.of(Messages.COMMAND_SYSTEM_ARROW_RIGHT, actor.getLanguage()).clickRun(arrowCommandFormat,
                commandManager.getPrefix(), helpText, page + 1).hoverText(Messages.COMMAND_SYSTEM_PAGE_PREVIOUS, actor.getLanguage()));
        }

        actor.sendTranslatedMessage(Messages.COMMAND_HELP_HEADER_FORMAT_START, Key.of("helpText", helpText), Key.of("page", page),
            Key.of("maxPage", maxPage));
        component.send(actor);
        actor.sendMessage(""); // Add one space
        int maxIndex = Math.min((page - 1) * HELP_PAGE_SIZE + HELP_PAGE_SIZE, tree.amount());
        for (int index = (page - 1) * HELP_PAGE_SIZE; index < maxIndex; index++) {
            HelpNodeTree.HelpNode helpNode = tree.path(index);
            NodeAction action = helpNode.node().getAction();
            List<NodeArgument> argumentList = action.getArguments().stream().filter(arg -> !arg.isProvided()).toList();
            if (argumentList.isEmpty()) {
                actor.sendTranslatedMessage(Messages.COMMAND_HELP_COMMAND_FORMAT_NOARGS, Key.of("prefix", prefix),
                    Key.of("name", helpNode.fullPath()));
                continue;
            }
            StringBuilder arguments = new StringBuilder();
            for (int i = 0; i < argumentList.size(); i++) {
                if (i != 0) {
                    arguments.append(' ');
                }
                NodeArgument argument = argumentList.get(index);
                MessageProvider provider = argument.isOptional() ? Messages.COMMAND_HELP_ARGUMENT_FORMAT_OPTIONAL
                    : Messages.COMMAND_HELP_ARGUMENT_FORMAT_REQUIRED;
                arguments.append(actor.getTranslatedMessageAsString(provider,
                    Key.of("type", ClassUtil.getClassName(argument.getArgumentType())), Key.of("name", argument.getName())));
            }
            actor.sendTranslatedMessage(Messages.COMMAND_HELP_COMMAND_FORMAT_WITHARGS, Key.of("prefix", prefix),
                Key.of("name", helpNode.fullPath()), Key.of("arguments", arguments.toString()),
                Key.of("description", action.getDescription()));
        }
        actor.sendMessage(""); // Add one space
        component.send(actor);
        actor.sendTranslatedMessage(Messages.COMMAND_HELP_HEADER_FORMAT_END, Key.of("helpText", helpText), Key.of("page", page),
            Key.of("maxPage", maxPage));
    }

    private String getPrefix(String prefix, Node command) {
        Node root = command.getRoot();
        if (root == command) {
            return prefix + command.getName();
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