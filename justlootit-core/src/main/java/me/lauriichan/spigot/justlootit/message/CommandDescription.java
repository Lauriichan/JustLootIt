package me.lauriichan.spigot.justlootit.message;

import java.util.Arrays;
import java.util.stream.Collectors;

import me.lauriichan.laylib.localization.source.IMessageDefinition;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.message.IMessageExtension;

@Extension
public enum CommandDescription implements IMessageDefinition, IMessageExtension {

    JUSTLOOTIT_PARENT("Main command for all JustLootIt commands"),
    
    JUSTLOOTIT_HELP_ALL("Gives all information about permitted commands"),
    JUSTLOOTIT_HELP_COMMAND("Gives all information about a specified permitted command"),

    JUSTLOOTIT_DEBUG_PDC("Reads all data from a block or entity"),
    
    JUSTLOOTIT_CONTAINER_INFO("Gets various details about the container linked to a block or entity"),
    JUSTLOOTIT_CONTAINER_SET("Links a container to a block or entity"),
    JUSTLOOTIT_CONTAINER_GROUP_SET("Sets the refresh group of a container linked to a block or entity"),
    JUSTLOOTIT_CONTAINER_GROUP_REMOVE("Removes the refresh group of a container linked to a block or entity"),
    JUSTLOOTIT_CONTAINER_CREATE_ANY("Creates a new container and links it to a block or entity"),
    JUSTLOOTIT_CONTAINER_CREATE_ENTITY("Creates a new container and links it to a block or entity while prioritizing entities over blocks"),
    JUSTLOOTIT_CONTAINER_MANAGE_ID("Manages a container by id"),
    JUSTLOOTIT_CONTAINER_MANAGE_LOCATION("Manages a container linked to a block or entity"),
    JUSTLOOTIT_CONTAINER_BULK_REPLACE_LOOTTABLE("Bulk replaces a loottable with a different one for each container"),
    JUSTLOOTIT_CONTAINER_BULK_RESET_ACCESS("Bulk resets access to all containers for all players"),
    
    JUSTLOOTIT_GROUP_CREATE("Creates a new refresh group"),
    JUSTLOOTIT_GROUP_DELETE("Deletes a refresh group"),
    JUSTLOOTIT_GROUP_LIST("Lists all refresh groups"),
    JUSTLOOTIT_GROUP_INFO("Gets the time and unit of a refresh group's interval"),
    JUSTLOOTIT_GROUP_SET_BOTH("Sets the time and unit of a refresh group's interval"),
    JUSTLOOTIT_GROUP_SET_UNIT("Sets the unit of a refresh group's interval and recalculates the time to match the unit"),
    JUSTLOOTIT_GROUP_SET_TIME("Sets the time of a refresh group's interval"),
    
    JUSTLOOTIT_CONFIG_SAVE("Saves all or just the specified config(s)"),
    JUSTLOOTIT_CONFIG_RELOAD("Reloads all or just the specified config(s)"),
    
    JUSTLOOTIT_CONVERT("Triggers a world conversion process")
    ;

    private final String id;
    private final String fallback;

    CommandDescription() {
        this("");
    }

    CommandDescription(final String[] fallback) {
        this(Arrays.stream(fallback).collect(Collectors.joining("\n")));
    }

    CommandDescription(final String fallback) {
        this.id = "command.description." + name().replace('$', '-').toLowerCase().replace('_', '.');
        this.fallback = fallback;
    }

    @Override
    public String fallback() {
        return fallback;
    }

    @Override
    public String id() {
        return id;
    }

}
