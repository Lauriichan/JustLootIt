package me.lauriichan.spigot.justlootit;

public final class JustLootItPermission {

    private JustLootItPermission() {
        throw new UnsupportedOperationException();
    }

    public static final String COMMAND_CONFIG = "justlootit.command.config";
    public static final String COMMAND_CONTAINER = "justlootit.command.container";
    public static final String COMMAND_DEBUG = "justlootit.command.debug";
    public static final String COMMAND_HELP = "justlootit.command.help";

    public static final String ACTION_REMOVE_CONTAINER_FRAME = "justlootit.remove.container.frame";
    public static final String ACTION_REMOVE_CONTAINER_ENTITY = "justlootit.remove.container.entity";
    public static final String ACTION_REMOVE_CONTAINER_BLOCK = "justlootit.remove.container.block";
    
}
