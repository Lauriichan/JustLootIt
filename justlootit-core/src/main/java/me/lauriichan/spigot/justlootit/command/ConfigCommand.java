package me.lauriichan.spigot.justlootit.command;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.annotation.Action;
import me.lauriichan.laylib.command.annotation.Argument;
import me.lauriichan.laylib.command.annotation.Command;
import me.lauriichan.laylib.command.annotation.Description;
import me.lauriichan.laylib.command.annotation.Permission;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.minecraft.pluginbase.config.ConfigManager;
import me.lauriichan.minecraft.pluginbase.config.ConfigWrapper;
import me.lauriichan.minecraft.pluginbase.config.IConfigWrapper;
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
    public void save(final Actor<?> actor,
        @Argument(name = "config", optional = true, index = 1) final IConfigWrapper<?> chosenWrapper,
        @Argument(name = "force", optional = true, index = 0) final boolean force) {
        if (chosenWrapper == null) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_SAVE_ALL_START);
            int reloaded = 0, skipped = 0;
            int amount = 0;
            for (final Entry<IConfigWrapper<?>, int[]> entry : configManager.save(force).object2ObjectEntrySet()) {
                final String configName = TypeName.ofConfig(entry.getKey());
                for (final int configState : entry.getValue()) {
                    amount++;
                    final int state = sendConfigSaveInfo(actor, configName, configState);
                    if (state == 1) {
                        reloaded++;
                    } else if (state == 0) {
                        skipped++;
                    }
                }
            }
            actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_SAVE_ALL_END, Key.of("total", amount), Key.of("success", reloaded),
                Key.of("skipped", skipped), Key.of("failed", amount - reloaded - skipped));
            return;
        }
        final String name = TypeName.ofConfig(chosenWrapper);
        actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_SAVE_CHOSEN_START, Key.of("config", name));
        int reloaded = 0, skipped = 0;
        int amount = 0;
        for (final int configState : chosenWrapper.save(force)) {
            amount++;
            final int state = sendConfigSaveInfo(actor, name, configState);
            if (state == 1) {
                reloaded++;
            } else if (state == 0) {
                skipped++;
            }
        }
        actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_SAVE_CHOSEN_END, Key.of("config", name), Key.of("total", amount),
            Key.of("success", reloaded), Key.of("skipped", skipped), Key.of("failed", amount - reloaded - skipped));
    }

    private static int sendConfigSaveInfo(final Actor<?> actor, final String name, final int state) {
        if (state == ConfigWrapper.SKIPPED) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_SAVE_RESULT_SKIPPED, Key.of("config", name));
            return 0;
        }
        if (state != ConfigWrapper.SUCCESS) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_SAVE_RESULT_FAILED, Key.of("config", name));
            return -1;
        }
        actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_SAVE_RESULT_SUCCESS, Key.of("config", name));
        return 1;
    }

    @Action("reload")
    @Description("$#command.description.justlootit.config.reload")
    public void reload(final Actor<?> actor,
        @Argument(name = "config", optional = true, index = 1) final IConfigWrapper<?> chosenWrapper,
        @Argument(name = "force", optional = true, index = 0) final boolean force) {
        if (chosenWrapper == null) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_RELOAD_ALL_START);
            int reloaded = 0, skipped = 0;
            int amount = 0;
            for (final Entry<IConfigWrapper<?>, int[]> entry : configManager.reload(force, false)
                .object2ObjectEntrySet()) {
                final String configName = TypeName.ofConfig(entry.getKey());
                for (final int configState : entry.getValue()) {
                    amount++;
                    final int state = sendConfigReloadInfo(actor, configName, configState);
                    if (state == 1) {
                        reloaded++;
                    } else if (state == 0) {
                        skipped++;
                    }
                }
            }
            actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_RELOAD_ALL_END, Key.of("total", amount), Key.of("success", reloaded),
                Key.of("skipped", skipped), Key.of("failed", amount - reloaded - skipped));
            return;
        }
        final String name = TypeName.ofConfig(chosenWrapper);
        actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_RELOAD_CHOSEN_START, Key.of("config", name));
        int reloaded = 0, skipped = 0;
        int amount = 0;
        for (final int configState : chosenWrapper.reload(force, false)) {
            amount++;
            final int state = sendConfigReloadInfo(actor, name, configState);
            if (state == 1) {
                reloaded++;
            } else if (state == 0) {
                skipped++;
            }
        }
        actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_RELOAD_CHOSEN_END, Key.of("config", name), Key.of("total", amount),
            Key.of("success", reloaded), Key.of("skipped", skipped), Key.of("failed", amount - reloaded - skipped));
    }

    private static int sendConfigReloadInfo(final Actor<?> actor, final String name, final int state) {
        if (state == ConfigWrapper.SKIPPED) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_RELOAD_RESULT_SKIPPED, Key.of("config", name));
            return 0;
        }
        if (state != ConfigWrapper.SUCCESS) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_RELOAD_RESULT_FAILED, Key.of("config", name));
            return -1;
        }
        actor.sendTranslatedMessage(Messages.COMMAND_CONFIG_RELOAD_RESULT_SUCCESS, Key.of("config", name));
        return 1;
    }

}
