package me.lauriichan.spigot.justlootit.command;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.annotation.Action;
import me.lauriichan.laylib.command.annotation.Argument;
import me.lauriichan.laylib.command.annotation.Command;
import me.lauriichan.laylib.command.annotation.Description;
import me.lauriichan.laylib.command.annotation.Permission;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.minecraft.pluginbase.config.ConfigManager;
import me.lauriichan.minecraft.pluginbase.config.ConfigWrapper;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.JustLootItPermission;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.message.Messages;
import me.lauriichan.spigot.justlootit.util.TypeName;

@Extension
@Command(name = "config")
@Permission(JustLootItPermission.COMMAND_CONFIG)
public class ConfigCommand implements ICommandExtension {

    private final ConfigManager configManager = JustLootItPlugin.get().configManager();

    @Action("save")
    @Description("$#command.description.justlootit.config.save")
    public void save(Actor<?> actor, @Argument(name = "config", optional = true, index = 1) ConfigWrapper<?> choosenWrapper,
        @Argument(name = "force", optional = true, index = 0) boolean force) {
        if (choosenWrapper == null) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_SAVE_ALL_START);
            int saved = 0, skipped = 0;
            int amount = 0;
            for (Object2IntMap.Entry<ConfigWrapper<?>> entry : configManager.save().object2IntEntrySet()) {
                amount++;
                int state = sendSaveInfo(actor, TypeName.ofConfig(entry.getKey().config()), entry.getIntValue());
                if (state == 1) {
                    saved++;
                } else if (state == 0) {
                    skipped++;
                }
            }
            actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_SAVE_ALL_END, Key.of("total", amount), Key.of("success", saved),
                Key.of("skipped", skipped), Key.of("failed", (amount - saved - skipped)));
            return;
        }
        String name = TypeName.ofConfig(choosenWrapper.config());
        actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_SAVE_SINGLE, Key.of("config", name));
        // Ignore state here as we only have one config.
        sendSaveInfo(actor, name, choosenWrapper.reload(force));
    }

    private static int sendSaveInfo(Actor<?> actor, String name, int state) {
        if (state == ConfigWrapper.SKIPPED) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_SAVE_RESULT_SKIPPED, Key.of("config", name));
            return 0;
        } else if (state != ConfigWrapper.SUCCESS) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_SAVE_RESULT_FAILED, Key.of("config", name));
            return -1;
        }
        actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_SAVE_RESULT_SUCCESS, Key.of("config", name));
        return 1;
    }

    @Action("reload")
    @Description("$#command.description.justlootit.config.reload")
    public void reload(Actor<?> actor, @Argument(name = "config", optional = true, index = 1) ConfigWrapper<?> choosenWrapper,
        @Argument(name = "force", optional = true, index = 0) boolean force) {
        if (choosenWrapper == null) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_RELOAD_ALL_START);
            int reloaded = 0, skipped = 0;
            int amount = 0;
            for (Object2IntMap.Entry<ConfigWrapper<?>> entry : configManager.reload().object2IntEntrySet()) {
                amount++;
                int state = sendReloadInfo(actor, TypeName.ofConfig(entry.getKey().config()), entry.getIntValue());
                if (state == 1) {
                    reloaded++;
                } else if (state == 0) {
                    skipped++;
                }
            }
            actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_RELOAD_ALL_END, Key.of("total", amount), Key.of("success", reloaded),
                Key.of("skipped", skipped), Key.of("failed", (amount - reloaded - skipped)));
            return;
        }
        String name = TypeName.ofConfig(choosenWrapper.config());
        actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_RELOAD_SINGLE, Key.of("config", name));
        // Ignore state here as we only have one config.
        sendReloadInfo(actor, name, choosenWrapper.reload(force));
    }

    private static int sendReloadInfo(Actor<?> actor, String name, int state) {
        if (state == ConfigWrapper.SKIPPED) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_RELOAD_RESULT_SKIPPED, Key.of("config", name));
            return 0;
        } else if (state != ConfigWrapper.SUCCESS) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_RELOAD_RESULT_FAILED, Key.of("config", name));
            return -1;
        }
        actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_RELOAD_RESULT_SUCCESS, Key.of("config", name));
        return 1;
    }

}
