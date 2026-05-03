package me.lauriichan.spigot.justlootit.command;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.annotation.Action;
import me.lauriichan.laylib.command.annotation.Argument;
import me.lauriichan.laylib.command.annotation.Command;
import me.lauriichan.laylib.command.annotation.Description;
import me.lauriichan.laylib.command.annotation.Permission;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.minecraft.pluginbase.data.DataManager;
import me.lauriichan.minecraft.pluginbase.data.IDataWrapper;
import me.lauriichan.minecraft.pluginbase.data.IDirectDataWrapper;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.JustLootItPermission;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.message.Messages;
import me.lauriichan.spigot.justlootit.nms.nbt.NbtHelper;
import me.lauriichan.spigot.justlootit.util.IOUtil;
import me.lauriichan.spigot.justlootit.util.TypeName;

@Extension
@Command(name = "data")
@Permission(JustLootItPermission.COMMAND_DATA)
public class DataCommand implements ICommandExtension {

    private final DataManager dataManager = JustLootItPlugin.get().dataManager();

    @Action("save")
    @Description("$#command.description.justlootit.data.save")
    public void save(final Actor<?> actor, @Argument(name = "data", optional = true, index = 1) final IDataWrapper<?, ?> chosenWrapper,
        @Argument(name = "force", optional = true, index = 0) final boolean force) {
        if (chosenWrapper == null) {
            actor.sendTranslatedMessage(Messages.COMMAND_DATA_SAVE_ALL_START);
            int reloaded = 0, skipped = 0;
            int amount = 0;
            for (final Entry<IDataWrapper<?, ?>, int[]> entry : dataManager.save(force).object2ObjectEntrySet()) {
                final String configName = TypeName.ofData(entry.getKey());
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
            actor.sendTranslatedMessage(Messages.COMMAND_DATA_SAVE_ALL_END, Key.of("total", amount), Key.of("success", reloaded),
                Key.of("skipped", skipped), Key.of("failed", amount - reloaded - skipped));
            return;
        }
        final String name = TypeName.ofData(chosenWrapper);
        actor.sendTranslatedMessage(Messages.COMMAND_DATA_SAVE_CHOSEN_START, Key.of("config", name));
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
        actor.sendTranslatedMessage(Messages.COMMAND_DATA_SAVE_CHOSEN_END, Key.of("config", name), Key.of("total", amount),
            Key.of("success", reloaded), Key.of("skipped", skipped), Key.of("failed", amount - reloaded - skipped));
    }

    private static int sendConfigSaveInfo(final Actor<?> actor, final String name, final int state) {
        if (state == IDirectDataWrapper.SKIPPED) {
            actor.sendTranslatedMessage(Messages.COMMAND_DATA_SAVE_RESULT_SKIPPED, Key.of("config", name));
            return 0;
        }
        if (state != IDirectDataWrapper.SUCCESS) {
            actor.sendTranslatedMessage(Messages.COMMAND_DATA_SAVE_RESULT_FAILED, Key.of("config", name));
            return -1;
        }
        actor.sendTranslatedMessage(Messages.COMMAND_DATA_SAVE_RESULT_SUCCESS, Key.of("config", name));
        return 1;
    }

    @Action("reload")
    @Description("$#command.description.justlootit.data.reload")
    public void reload(final Actor<?> actor, @Argument(name = "data", optional = true, index = 1) final IDataWrapper<?, ?> chosenWrapper,
        @Argument(name = "force", optional = true, index = 0) final boolean force) {
        if (chosenWrapper == null) {
            actor.sendTranslatedMessage(Messages.COMMAND_DATA_RELOAD_ALL_START);
            int reloaded = 0, skipped = 0;
            int amount = 0;
            for (final Entry<IDataWrapper<?, ?>, int[]> entry : dataManager.reload(force, false).object2ObjectEntrySet()) {
                final String configName = TypeName.ofData(entry.getKey());
                for (final int configState : entry.getValue()) {
                    amount++;
                    final int state = sendDataReloadInfo(actor, configName, configState);
                    if (state == 1) {
                        reloaded++;
                    } else if (state == 0) {
                        skipped++;
                    }
                }
            }
            actor.sendTranslatedMessage(Messages.COMMAND_DATA_RELOAD_ALL_END, Key.of("total", amount), Key.of("success", reloaded),
                Key.of("skipped", skipped), Key.of("failed", amount - reloaded - skipped));
            return;
        }
        final String name = TypeName.ofData(chosenWrapper);
        actor.sendTranslatedMessage(Messages.COMMAND_DATA_RELOAD_CHOSEN_START, Key.of("config", name));
        int reloaded = 0, skipped = 0;
        int amount = 0;
        for (final int configState : chosenWrapper.reload(force, false)) {
            amount++;
            final int state = sendDataReloadInfo(actor, name, configState);
            if (state == 1) {
                reloaded++;
            } else if (state == 0) {
                skipped++;
            }
        }
        actor.sendTranslatedMessage(Messages.COMMAND_DATA_RELOAD_CHOSEN_END, Key.of("config", name), Key.of("total", amount),
            Key.of("success", reloaded), Key.of("skipped", skipped), Key.of("failed", amount - reloaded - skipped));
    }

    private static int sendDataReloadInfo(final Actor<?> actor, final String name, final int state) {
        if (state == IDirectDataWrapper.SKIPPED) {
            actor.sendTranslatedMessage(Messages.COMMAND_DATA_RELOAD_RESULT_SKIPPED, Key.of("config", name));
            return 0;
        }
        if (state != IDirectDataWrapper.SUCCESS) {
            actor.sendTranslatedMessage(Messages.COMMAND_DATA_RELOAD_RESULT_FAILED, Key.of("config", name));
            return -1;
        }
        actor.sendTranslatedMessage(Messages.COMMAND_DATA_RELOAD_RESULT_SUCCESS, Key.of("config", name));
        return 1;
    }

    @Action("hand nbt")
    @Description("$#command.description.justlootit.data.hand.nbt")
    public void handNbt(final JustLootItPlugin plugin, final Actor<?> rawActor) {
        Actor<Player> actor = rawActor.as(Player.class);
        if (!actor.isValid()) {
            actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ACTOR_NOT_SUPPORTED, Key.of("actorType", "Player"));
            return;
        }
        Player player = actor.getHandle();
        ItemStack offHand = player.getInventory().getItem(EquipmentSlot.OFF_HAND);
        ItemStack hand = player.getInventory().getItem(EquipmentSlot.HAND);

        NbtHelper nbtHelper = plugin.versionHandler().nbtHelper();

        boolean anySend = false;
        if (hand != null && !hand.getType().isAir()) {
            actor.actionMessageBuilder()
                .action(me.lauriichan.laylib.command.Action.copy(IOUtil.toString(nbtHelper.asJson(nbtHelper.asTag(hand)))))
                .message(Messages.COMMAND_DATA_HAND_NBT_MAIN_HAND).send(actor);
            anySend = true;
        }
        if (offHand != null && !offHand.getType().isAir()) {
            actor.actionMessageBuilder()
                .action(me.lauriichan.laylib.command.Action.copy(IOUtil.toString(nbtHelper.asJson(nbtHelper.asTag(offHand)))))
                .message(Messages.COMMAND_DATA_HAND_NBT_MAIN_HAND).send(actor);
            anySend = true;
        }
        if (anySend) {
            return;
        }
        actor.sendTranslatedMessage(Messages.COMMAND_DATA_HAND_NBT_NO_HAND);
    }

}
