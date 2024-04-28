package me.lauriichan.spigot.justlootit.command;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.annotation.Action;
import me.lauriichan.laylib.command.annotation.Command;
import me.lauriichan.laylib.command.annotation.Permission;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.JustLootItPermission;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.convert.ConvProp;
import me.lauriichan.spigot.justlootit.convert.ConversionProperties;
import me.lauriichan.spigot.justlootit.input.SimpleChatInputProvider;
import me.lauriichan.spigot.justlootit.message.Messages;

@Extension
@Command(name = "convert")
@Permission(JustLootItPermission.COMMAND_CONVERT)
public class ConvertCommand implements ICommandExtension {

    private final SimpleChatInputProvider inputProvider = SimpleChatInputProvider.CHAT;

    private volatile ConversionProperties properties;

    @Action("")
    public void convert(final JustLootItPlugin plugin, Actor<?> actor) {
        if (!actor.as(ConsoleCommandSender.class).isValid()) {
            actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ACTOR_NOT_SUPPORTED, Key.of("actorType", "Console"));
            return;
        }
        if (properties != null) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONVERT_PROCESS_ONGOING);
            return;
        }
        properties = new ConversionProperties(plugin.logger(), plugin.getConversionPropertyFile(), true);
        inputProvider.getBooleanInput(actor, actor.getTranslatedMessageAsString(Messages.INPUT_PROMPT_CONVERT_DO_LOOTIN),
            actor.getTranslatedMessageAsString(Messages.INPUT_RETRY_BOOLEAN), this::propertyDoLootin);
    }

    private void propertyDoLootin(Actor<?> actor, Boolean state) {
        if (state == null) {
            properties = null;
            return;
        }
        properties.setProperty(ConvProp.DO_LOOTIN_CONVERSION, state);
        if (state) {
            inputProvider.getBooleanInput(actor, actor.getTranslatedMessageAsString(Messages.INPUT_PROMPT_CONVERT_LOOTIN_STATIC),
                actor.getTranslatedMessageAsString(Messages.INPUT_RETRY_BOOLEAN), this::propertyLootinStatic);
        } else {
            inputProvider.getBooleanInput(actor, actor.getTranslatedMessageAsString(Messages.INPUT_PROMPT_CONVERT_DO_VANILLA),
                actor.getTranslatedMessageAsString(Messages.INPUT_RETRY_BOOLEAN), this::propertyDoVanilla);
        }
    }

    private void propertyLootinStatic(Actor<?> actor, Boolean state) {
        if (state == null) {
            properties = null;
            return;
        }
        properties.setProperty(ConvProp.LOOTIN_DISABLE_STATIC_CONTAINER, state);
        inputProvider.getBooleanInput(actor, actor.getTranslatedMessageAsString(Messages.INPUT_PROMPT_CONVERT_DO_VANILLA),
            actor.getTranslatedMessageAsString(Messages.INPUT_RETRY_BOOLEAN), this::propertyDoVanilla);
    }

    private void propertyDoVanilla(Actor<?> actor, Boolean state) {
        if (state == null) {
            properties = null;
            return;
        }
        properties.setProperty(ConvProp.DO_VANILLA_CONVERSION, state);
        if (state) {
            inputProvider.getBooleanInput(actor,
                actor.getTranslatedMessageAsString(Messages.INPUT_PROMPT_CONVERT_VANILLA_STATIC_CONTAINERS),
                actor.getTranslatedMessageAsString(Messages.INPUT_RETRY_BOOLEAN), this::propertyVanillaStatic);
        } else {
            runConversion(actor);
        }
    }

    private void propertyVanillaStatic(Actor<?> actor, Boolean state) {
        if (state == null) {
            properties = null;
            return;
        }
        properties.setProperty(ConvProp.VANILLA_ALLOW_STATIC_CONTAINER, state);
        inputProvider.getBooleanInput(actor, actor.getTranslatedMessageAsString(Messages.INPUT_PROMPT_CONVERT_VANILLA_ITEM_FRAMES),
            actor.getTranslatedMessageAsString(Messages.INPUT_RETRY_BOOLEAN), this::propertyVanillaItemFrames);
    }

    private void propertyVanillaItemFrames(Actor<?> actor, Boolean state) {
        if (state == null) {
            properties = null;
            return;
        }
        properties.setProperty(ConvProp.VANILLA_ALLOW_ITEM_FRAME, state);
        if (state) {
            inputProvider.getBooleanInput(actor,
                actor.getTranslatedMessageAsString(Messages.INPUT_PROMPT_CONVERT_VANILLA_ELYTRA_FRAMES_ONLY),
                actor.getTranslatedMessageAsString(Messages.INPUT_RETRY_BOOLEAN), this::propertyVanillaElytraFrames);
        } else {
            runConversion(actor);
        }
    }

    private void propertyVanillaElytraFrames(Actor<?> actor, Boolean state) {
        if (state == null) {
            properties = null;
            return;
        }
        properties.setProperty(ConvProp.VANILLA_ALLOW_ONLY_ELYTRA_FRAME, state);
        runConversion(actor);
    }

    private void runConversion(Actor<?> actor) {
        properties.save();
        actor.sendTranslatedMessage(Messages.COMMAND_CONVERT_PROCESS_DONE);
        JustLootItPlugin.get().scheduler().syncLater(() -> Bukkit.getServer().spigot().restart(), 100);
    }

}
