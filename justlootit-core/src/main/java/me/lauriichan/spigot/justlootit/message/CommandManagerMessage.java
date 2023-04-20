package me.lauriichan.spigot.justlootit.message;

import java.util.Arrays;
import java.util.stream.Collectors;

import me.lauriichan.laylib.localization.source.IMessageDefinition;

public enum CommandManagerMessage implements IMessageDefinition {

    CANCEL_UNAVAILABLE("$#plugin.prefix You currently have no active command process to cancel!"),
    CANCEL_SUCCESS("$#plugin.prefix Successfully cancelled the process of command '&c$command&7'!"),

    CREATE_NO$COMMAND("$#plugin.prefix The command '&c$command&7' doesn't exist!"),
    CREATE_NO$ACTION("$#plugin.prefix The command '&c$command&7' has no action to be executed!"),
    CREATE_SUCCESS("$#plugin.prefix Successfully started command process for command '&c$command&7'!"),

    SKIP_UNSKIPPABLE("$#plugin.prefix The argument '&c$argument&7' is required to execute the command '&c$command&7'!"),
    SKIP_SUCCESS("$#plugin.prefix Successfully skipped the argument '&c$argument&7'!"),

    INPUT_SUGGESTION("&cSuggestion &8>> &7$input"),
    INPUT_USER("&cYou &8>> &7$input"),
    INPUT_FAILED(new String[] {
        "$#plugin.prefix Failed to parse input for argument of type &c$argument.type&7!",
        "$#plugin.prefix &c$error"
    }),

    SUGGEST_HEADER("$#plugin.prefix Did you mean any of these suggestions?"),
    SUGGEST_FORMAT("&8[&e$certainty&8]: &7$suggestion"),
    SUGGEST_HOVER("&7Click to enter this suggestion"),

    ARGUMENT_SPECIFY("$#plugin.prefix Please enter the argument '&c$argument.name&7' of type &c$argument.type&7!"),
    ARGUMENT_CANCELABLE_MESSAGE("$#plugin.prefix You can cancel this process using &c/cancel&7!"),
    ARGUMENT_CANCELABLE_HOVER("&7Click to use &c/cancel"),
    ARGUMENT_OPTIONAL_MESSAGE("$#plugin.prefix This argument is optional and can be skipped using &c/skip&7!"),
    ARGUMENT_OPTIONAL_HOVER("&7Click to use &c/skip"),

    EXECUTION_FAILED(new String[] {
        "$#plugin.prefix An error occured while executing command '&c$command&7'!",
        "$#plugin.prefix &4Error: &c$error"
    }),

    NOT$PERMITTED("$#plugin.prefix You are lacking the permission &c$permission &7to do this.");

    private final String id;
    private final String fallback;

    CommandManagerMessage() {
        this("");
    }

    CommandManagerMessage(final String[] fallback) {
        this(Arrays.stream(fallback).collect(Collectors.joining("\n")));
    }

    CommandManagerMessage(final String fallback) {
        this.id = "command.process." + name().replace('$', '-').toLowerCase().replace('_', '.');
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