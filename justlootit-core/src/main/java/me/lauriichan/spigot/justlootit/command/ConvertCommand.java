package me.lauriichan.spigot.justlootit.command;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.UUID;

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
import me.lauriichan.spigot.justlootit.capability.ActorCapability;
import me.lauriichan.spigot.justlootit.command.impl.LootItActor;
import me.lauriichan.spigot.justlootit.convert.ConvProp;
import me.lauriichan.spigot.justlootit.convert.ConversionProperties;
import me.lauriichan.spigot.justlootit.input.SimpleChatInputProvider;
import me.lauriichan.spigot.justlootit.message.Messages;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;

@Extension
@Command(name = "convert")
@Permission(JustLootItPermission.COMMAND_CONVERT)
public class ConvertCommand implements ICommandExtension {

    private static final String ATTR_PROPERTIES = "conversion_properties";

    private final SimpleChatInputProvider inputProvider = SimpleChatInputProvider.CHAT;

    private volatile OffsetDateTime requestExpiry;
    private volatile UUID request;

    private volatile UUID conversionSetup;

    @Action("")
    public void convert(final JustLootItPlugin plugin, Actor<?> actor) {
        if (conversionSetup != null) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONVERT_PROCESS_ONGOING, Key.of("name", actorName(conversionSetup, plugin)));
            return;
        }
        if (!actor.as(ConsoleCommandSender.class).isValid()) {
            if (requestExpiry != null && !OffsetDateTime.now().isAfter(requestExpiry)) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONVERT_PROCESS_ONGOING, Key.of("name", actorName(request, plugin)));
                return;
            }
            requestExpiry = OffsetDateTime.now().plusMinutes(3);
            request = actor.getId();
            actor.sendTranslatedMessage(Messages.COMMAND_CONVERT_PROCESS_USER_REQUEST);
            return;
        }
        if (request != null) {
            if (requestExpiry != null && OffsetDateTime.now().isAfter(requestExpiry)) {
                requestExpiry = null;
                request = null;
                actor.sendTranslatedMessage(Messages.COMMAND_CONVERT_PROCESS_USER_EXPIRED);
                return;
            }
            PlayerAdapter adapter = plugin.versionHandler().getPlayer(request);
            if (adapter == null) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONVERT_PROCESS_USER_EXPIRED);
                return;
            }
            actor.sendTranslatedMessage(Messages.COMMAND_CONVERT_PROCESS_USER_CONFIRMED_CONSOLE, Key.of("name", adapter.getName()));
            conversionSetup = adapter.getUniqueId();
            request = null;
            requestExpiry = null;
            actor = ActorCapability.actor(adapter);
            actor.sendTranslatedMessage(Messages.COMMAND_CONVERT_PROCESS_USER_CONFIRMED_PLAYER);
        } else {
            conversionSetup = actor.getId();
        }
        newProperties(plugin, actor);
        inputProvider.getBooleanInput(actor, actor.getTranslatedMessageAsString(Messages.INPUT_PROMPT_CONVERT_DO_LOOTIN),
            actor.getTranslatedMessageAsString(Messages.INPUT_RETRY_BOOLEAN), this::propertyDoLootin);
    }
    
    private String actorName(final UUID uuid, final JustLootItPlugin plugin) {
        if (uuid == Actor.IMPL_ID) {
            return Bukkit.getConsoleSender().getName();
        }
        return plugin.versionHandler().getPlayer(uuid).getName();
    }

    private void newProperties(final JustLootItPlugin plugin, Actor<?> actor) {
        ((LootItActor<?>) actor).attributes().attrSet(ATTR_PROPERTIES,
            new ConversionProperties(plugin.logger(), plugin.getConversionPropertyFile(), true));
    }

    private ConversionProperties properties(Actor<?> actor) {
        return ((LootItActor<?>) actor).attributes().attr(ATTR_PROPERTIES, ConversionProperties.class);
    }

    private void clearProperties(Actor<?> actor) {
        conversionSetup = null;
        ((LootItActor<?>) actor).attributes().attrUnset(ATTR_PROPERTIES);
    }

    private void propertyDoLootin(Actor<?> actor, Boolean state) {
        if (state == null) {
            clearProperties(actor);
            return;
        }
        properties(actor).setProperty(ConvProp.DO_LOOTIN_CONVERSION, state);
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
            clearProperties(actor);
            return;
        }
        properties(actor).setProperty(ConvProp.LOOTIN_DISABLE_STATIC_CONTAINER, state);
        inputProvider.getBooleanInput(actor, actor.getTranslatedMessageAsString(Messages.INPUT_PROMPT_CONVERT_DO_VANILLA),
            actor.getTranslatedMessageAsString(Messages.INPUT_RETRY_BOOLEAN), this::propertyDoVanilla);
    }

    private void propertyDoVanilla(Actor<?> actor, Boolean state) {
        if (state == null) {
            clearProperties(actor);
            return;
        }
        properties(actor).setProperty(ConvProp.DO_VANILLA_CONVERSION, state);
        if (state) {
            inputProvider.getBooleanInput(actor,
                actor.getTranslatedMessageAsString(Messages.INPUT_PROMPT_CONVERT_VANILLA_STATIC_CONTAINERS),
                actor.getTranslatedMessageAsString(Messages.INPUT_RETRY_BOOLEAN), this::propertyVanillaStatic);
        } else {
            migrationPrompt(actor);
        }
    }

    private void propertyVanillaStatic(Actor<?> actor, Boolean state) {
        if (state == null) {
            clearProperties(actor);
            return;
        }
        properties(actor).setProperty(ConvProp.VANILLA_ALLOW_STATIC_CONTAINER, state);
        inputProvider.getBooleanInput(actor, actor.getTranslatedMessageAsString(Messages.INPUT_PROMPT_CONVERT_VANILLA_ITEM_FRAMES),
            actor.getTranslatedMessageAsString(Messages.INPUT_RETRY_BOOLEAN), this::propertyVanillaItemFrames);
    }

    private void propertyVanillaItemFrames(Actor<?> actor, Boolean state) {
        if (state == null) {
            clearProperties(actor);
            return;
        }
        properties(actor).setProperty(ConvProp.VANILLA_ALLOW_ITEM_FRAME, state);
        if (state) {
            inputProvider.getBooleanInput(actor,
                actor.getTranslatedMessageAsString(Messages.INPUT_PROMPT_CONVERT_VANILLA_ELYTRA_FRAMES_ONLY),
                actor.getTranslatedMessageAsString(Messages.INPUT_RETRY_BOOLEAN), this::propertyVanillaElytraFrames);
        } else {
            migrationPrompt(actor);
        }
    }

    private void propertyVanillaElytraFrames(Actor<?> actor, Boolean state) {
        if (state == null) {
            clearProperties(actor);
            return;
        }
        properties(actor).setProperty(ConvProp.VANILLA_ALLOW_ONLY_ELYTRA_FRAME, state);
        migrationPrompt(actor);
    }

    private void migrationPrompt(Actor<?> actor) {
        inputProvider.getBooleanInput(actor, actor.getTranslatedMessageAsString(Messages.INPUT_PROMPT_CONVERT_DO_MIGRATION), null,
            this::propertyMigration);
    }

    private void propertyMigration(Actor<?> actor, Boolean state) {
        if (state == null) {
            clearProperties(actor);
            return;
        }
        properties(actor).setProperty(ConvProp.DO_MIGRATION_CONVERSION, state);
        inputProvider.getStringInput(actor, actor.getTranslatedMessageAsString(Messages.INPUT_PROMPT_CONVERT_BLACKLIST_WORLD_INFO), null,
            this::blacklistWorldSubmit);
    }

    private void blacklistWorldSubmit(Actor<?> actor, String worldName) {
        if (worldName.equalsIgnoreCase("#start") || worldName.isBlank()) {
            runConversion(actor);
            return;
        }
        File world = new File(Bukkit.getWorldContainer(), worldName);
        if (!world.isDirectory()) {
            inputProvider.getStringInput(actor,
                actor.getTranslatedMessageAsString(Messages.INPUT_PROMPT_CONVERT_BLACKLIST_WORLD_FAILED, Key.of("worldName", worldName)),
                null, this::blacklistWorldSubmit);
        } else {
            properties(actor).addPropertyEntry(ConvProp.BLACKLISTED_WORLDS, worldName);
            inputProvider.getStringInput(actor,
                actor.getTranslatedMessageAsString(Messages.INPUT_PROMPT_CONVERT_BLACKLIST_WORLD_ADDED, Key.of("worldName", worldName)),
                null, this::blacklistWorldSubmit);
        }
    }

    private void runConversion(Actor<?> actor) {
        properties(actor).save();
        actor.sendTranslatedMessage(Messages.COMMAND_CONVERT_PROCESS_DONE);
        JustLootItPlugin.get().scheduler().syncLater(() -> Bukkit.getServer().spigot().restart(), 100);
    }

}
