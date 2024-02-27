package me.lauriichan.spigot.justlootit.command;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import it.unimi.dsi.fastutil.objects.ObjectCollection;
import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.CommandManager;
import me.lauriichan.laylib.command.annotation.Action;
import me.lauriichan.laylib.command.annotation.Argument;
import me.lauriichan.laylib.command.annotation.Command;
import me.lauriichan.laylib.command.annotation.Description;
import me.lauriichan.laylib.command.annotation.Param;
import me.lauriichan.laylib.command.annotation.Permission;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.message.component.Component;
import me.lauriichan.minecraft.pluginbase.message.component.ComponentCompound;
import me.lauriichan.spigot.justlootit.JustLootItPermission;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.config.RefreshConfig;
import me.lauriichan.spigot.justlootit.config.data.RefreshGroup;
import me.lauriichan.spigot.justlootit.message.Messages;
import me.lauriichan.spigot.justlootit.util.DataHelper;

@Extension
@Command(name = "group")
@Permission(JustLootItPermission.COMMAND_GROUP)
public final class GroupCommand implements ICommandExtension {

    public static final int GROUP_PAGE_SIZE = 9;

    private final RefreshConfig config = JustLootItPlugin.get().configManager().config(RefreshConfig.class);

    @Action("create")
    @Description("$#command.description.justlootit.group.create")
    public void create(Actor<?> actor, @Argument(name = "group id", index = 0) String groupId,
        @Argument(name = "time", index = 1, params = @Param(name = "minimum", longValue = 0, type = Param.TYPE_LONG)) long value,
        @Argument(name = "unit", index = 2, params = @Param(name = "type", classValue = TimeUnit.class, type = Param.TYPE_CLASS)) TimeUnit timeUnit) {
        if (config.group(groupId) != null) {
            actor.sendTranslatedMessage(Messages.COMMAND_GROUP_CREATE_ALREADY_EXISTS, Key.of("group", groupId));
            return;
        }
        if (timeUnit == TimeUnit.MICROSECONDS || timeUnit == TimeUnit.NANOSECONDS) {
            actor.sendTranslatedMessage(Messages.COMMAND_GROUP_ALL_UNSUPPORTED, Key.of("unit", timeUnit.name().toLowerCase()));
            return;
        }
        RefreshGroup group = config.getOrCreateGroup(groupId);
        group.set(value, timeUnit);
        actor.sendTranslatedMessage(Messages.COMMAND_GROUP_CREATE_SUCCESS, Key.of("group", groupId),
            getTimePlaceholder(actor, group.timeoutTime(), timeUnit));
    }

    @Action("set time")
    @Description("$#command.description.justlootit.group.set.time")
    public void set(Actor<?> actor, @Argument(name = "group", index = 0) RefreshGroup group,
        @Argument(name = "time", index = 1, params = @Param(name = "minimum", longValue = 0, type = Param.TYPE_LONG)) long value) {
        group.timeoutTime(value);
        actor.sendTranslatedMessage(Messages.COMMAND_GROUP_SET, Key.of("group", group.id()),
            getTimePlaceholder(actor, group.timeoutTime(), group.unit()));
    }

    @Action("set unit")
    @Description("$#command.description.justlootit.group.set.unit")
    public void set(Actor<?> actor, @Argument(name = "group", index = 0) RefreshGroup group,
        @Argument(name = "unit", index = 1, params = @Param(name = "type", classValue = TimeUnit.class, type = Param.TYPE_CLASS)) TimeUnit timeUnit) {
        if (timeUnit == TimeUnit.MICROSECONDS || timeUnit == TimeUnit.NANOSECONDS) {
            actor.sendTranslatedMessage(Messages.COMMAND_GROUP_ALL_UNSUPPORTED, Key.of("unit", timeUnit.name().toLowerCase()));
            return;
        }
        group.unit(timeUnit);
        actor.sendTranslatedMessage(Messages.COMMAND_GROUP_SET, Key.of("group", group.id()),
            getTimePlaceholder(actor, group.timeoutTime(), timeUnit));
    }

    @Action("set")
    @Description("$#command.description.justlootit.group.set.both")
    public void set(Actor<?> actor, @Argument(name = "group", index = 0) RefreshGroup group,
        @Argument(name = "time", index = 1, params = @Param(name = "minimum", longValue = 0, type = Param.TYPE_LONG)) long value,
        @Argument(name = "unit", index = 2, params = @Param(name = "type", classValue = TimeUnit.class, type = Param.TYPE_CLASS)) TimeUnit timeUnit) {
        if (timeUnit == TimeUnit.MICROSECONDS || timeUnit == TimeUnit.NANOSECONDS) {
            actor.sendTranslatedMessage(Messages.COMMAND_GROUP_ALL_UNSUPPORTED, Key.of("unit", timeUnit.name().toLowerCase()));
            return;
        }
        group.set(value, timeUnit);
        actor.sendTranslatedMessage(Messages.COMMAND_GROUP_SET, Key.of("group", group.id()),
            getTimePlaceholder(actor, group.timeoutTime(), timeUnit));
    }

    @Action("delete")
    @Description("$#command.description.justlootit.group.delete")
    public void delete(Actor<?> actor, @Argument(name = "group", index = 0) RefreshGroup group) {
        config.deleteGroup(group.id());
        actor.sendTranslatedMessage(Messages.COMMAND_GROUP_DELETE, Key.of("group", group.id()));
    }

    @Action("info")
    @Description("$#command.description.justlootit.group.info")
    public void info(Actor<?> actor, @Argument(name = "group", index = 0) RefreshGroup group) {
        actor.sendTranslatedMessage(Messages.COMMAND_GROUP_INFO, Key.of("group", group.id()),
            getTimePlaceholder(actor, group.timeoutTime(), group.unit()));
    }

    @Action("list")
    @Description("$#command.description.justlootit.group.list")
    public void list(CommandManager commandManager, Actor<?> actor, @Argument(name = "page", index = 0, optional = true) int page) {
        ObjectCollection<RefreshGroup> groupsCollection = config.groups();
        if (groupsCollection.isEmpty()) {
            actor.sendTranslatedMessage(Messages.COMMAND_GROUP_LIST_NO_ENTRIES);
            return;
        }
        RefreshGroup[] groups = groupsCollection.toArray(RefreshGroup[]::new);
        Arrays.sort(groups, (g1, g2) -> g1.id().compareTo(g2.id()));
        int maxPage = Math.floorDiv(groups.length, GROUP_PAGE_SIZE) + (groups.length % GROUP_PAGE_SIZE != 0 ? 1 : 0);
        page = Math.min(Math.max(page, 1), maxPage);

        actor.sendTranslatedMessage(Messages.COMMAND_GROUP_LIST_FORMAT_HEADER, Key.of("page", page), Key.of("maxPage", maxPage));
        actor.sendMessage(""); // Add one space
        int maxIndex = Math.min((page - 1) * GROUP_PAGE_SIZE + GROUP_PAGE_SIZE, groups.length);
        RefreshGroup group;
        for (int index = (page - 1) * GROUP_PAGE_SIZE; index < maxIndex; index++) {
            group = groups[index];
            Component
                .of(actor.getTranslatedMessageAsString(Messages.COMMAND_GROUP_LIST_FORMAT_ENTRY_TEXT, Key.of("group", group.id()),
                    getTimePlaceholder(actor, group.timeoutTime(), group.unit())))
                .hoverText(Messages.COMMAND_GROUP_LIST_FORMAT_ENTRY_HOVER, actor.getLanguage())
                .clickSuggest("{0} set {1}", commandManager.getPrefix() + "group", group.id()).send(actor);
        }
        actor.sendMessage(""); // Add one space
        if (actor.getId() != Actor.IMPL_ID) {
            ComponentCompound component = ComponentCompound.create();
            if (page != 1) {
                component.add(Component.of(Messages.COMMAND_SYSTEM_ARROW_LEFT, actor.getLanguage())
                    .clickRun("{0} list {1}", commandManager.getPrefix() + "group", page - 1)
                    .hoverText(Messages.COMMAND_SYSTEM_PAGE_PREVIOUS, actor.getLanguage()));
            }
            if (page != maxPage) {
                if (page != 1) {
                    component.add(Component.of(Messages.COMMAND_SYSTEM_ARROW_SEPERATOR, actor.getLanguage()));
                }
                component.add(Component.of(Messages.COMMAND_SYSTEM_ARROW_RIGHT, actor.getLanguage())
                    .clickRun("{0} list {1}", commandManager.getPrefix() + "group", page + 1)
                    .hoverText(Messages.COMMAND_SYSTEM_PAGE_NEXT, actor.getLanguage()));
            }
            if (!component.isEmpty()) {
                component.send(actor);
            }
        }
        actor.sendTranslatedMessage(Messages.COMMAND_GROUP_LIST_FORMAT_HEADER, Key.of("page", page), Key.of("maxPage", maxPage));
    }

    private Key getTimePlaceholder(Actor<?> actor, long time, TimeUnit unit) {
        if (time == 0) {
            return Key.of("time", actor.getTranslatedMessageAsString(Messages.COMMAND_GROUP_ALL_NEVER));
        }
        return Key.of("time", actor.getTranslatedMessageAsString(DataHelper.providerByUnit(unit), Key.of("value", time)));
    }

}
