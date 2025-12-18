package me.lauriichan.spigot.justlootit;

public final class JustLootItPermission {

    private JustLootItPermission() {
        throw new UnsupportedOperationException();
    }

    public static final String COMMAND_UNINSTALL = "justlootit.command.uninstall";
    public static final String COMMAND_CONVERT = "justlootit.command.convert";
    public static final String COMMAND_CONFIG = "justlootit.command.config";
    public static final String COMMAND_CONTAINER = "justlootit.command.container";
    public static final String COMMAND_GROUP = "justlootit.command.group";
    public static final String COMMAND_DEBUG = "justlootit.command.debug";
    public static final String COMMAND_HELP = "justlootit.command.help";

    public static final String ADMIN_INFORM_VERSION = "justlootit.admin.check_version";

    public static final String ACTION_REMOVE_CONTAINER_ENTITY = "justlootit.remove.container.entity";
    public static final String ACTION_REMOVE_CONTAINER_BLOCK = "justlootit.remove.container.block";

}
