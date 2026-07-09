package me.lauriichan.spigot.justlootit.command.impl;

import java.time.Duration;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.lauriichan.laylib.localization.IMessage;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.laylib.localization.MessageProvider;
import me.lauriichan.minecraft.pluginbase.message.component.ComponentBuilder;
import me.lauriichan.minecraft.pluginbase.message.component.SubComponentBuilder;
import me.lauriichan.spigot.justlootit.storage.util.Tuple;
import me.lauriichan.spigot.justlootit.util.DataHelper;
import me.lauriichan.spigot.justlootit.util.progress.IDoneNotifier;
import me.lauriichan.spigot.justlootit.util.progress.IProgressNotifier;
import me.lauriichan.spigot.justlootit.util.progress.ProgressTracker;
import net.md_5.bungee.api.ChatMessageType;

public final class ActorNotifierBuilder<P extends CommandSender> {

    private final LootItActor<P> actor;
    private final boolean isPlayer;

    private MessageProvider progressMessage, detailedProgressMessage, doneMessage;
    private int messageUpdateDelay = 0;

    private ChatMessageType messageType = ChatMessageType.CHAT;
    private boolean useBossBar = false;

    ActorNotifierBuilder(LootItActor<P> actor) {
        this.actor = Objects.requireNonNull(actor);
        this.isPlayer = actor.as(Player.class).isValid();
    }

    public ActorNotifierBuilder<P> messageType(ChatMessageType messageType) {
        if (messageType != null && isPlayer) {
            this.messageType = messageType;
            return this;
        }
        this.messageType = ChatMessageType.CHAT;
        return this;
    }

    public ChatMessageType messageType() {
        return messageType;
    }

    public ActorNotifierBuilder<P> useBossBar(boolean useBossBar) {
        this.useBossBar = useBossBar;
        return this;
    }

    public boolean useBossBar() {
        return useBossBar;
    }

    // Only relevant for boss bar
    public ActorNotifierBuilder<P> messageUpdateDelay(int messageUpdateDelay) {
        this.messageUpdateDelay = Math.max(messageUpdateDelay, 0);
        return this;
    }

    public int messageUpdateDelay() {
        return messageUpdateDelay;
    }

    public ActorNotifierBuilder<P> progressMessage(MessageProvider progressMessage) {
        this.progressMessage = progressMessage;
        return this;
    }

    public MessageProvider progressMessage() {
        return progressMessage;
    }

    public ActorNotifierBuilder<P> detailedProgressMessage(MessageProvider detailedProgressMessage) {
        this.detailedProgressMessage = detailedProgressMessage;
        return this;
    }

    public MessageProvider detailedProgressMessage() {
        return detailedProgressMessage;
    }

    public ActorNotifierBuilder<P> doneMessage(MessageProvider doneMessage) {
        this.doneMessage = doneMessage;
        return this;
    }

    public MessageProvider doneMessage() {
        return doneMessage;
    }

    public ProgressTracker buildTracker(Key... placeholders) {
        return new ProgressTracker().notifiers(build(placeholders));
    }

    public Tuple<IProgressNotifier, IDoneNotifier> build(Key... placeholders) {
        if (progressMessage == null) {
            throw new IllegalArgumentException("Progress message not set");
        }
        final ChatMessageType messageType = this.messageType;
        final LootItActor<P> actor = this.actor;
        final IMessage doneMessage = translate(this.doneMessage), progressMessage = translate(this.progressMessage),
            detailedProgressMessage = translate(this.detailedProgressMessage);

        IDoneNotifier doneNotifier = null;
        IProgressNotifier progressNotifier;

        boolean actuallyUseBossBar = actor.isPlayer() && useBossBar;
        if (actuallyUseBossBar) {
            final int ticks = this.messageUpdateDelay;
            CommandSender console = Bukkit.getConsoleSender();
            progressNotifier = IProgressNotifier.attributed((attributes, progress, elapsed, detailed) -> {
                if (!attributes.attrHas("setup")) {
                    BossBar bossBar = actor.bossBar();
                    bossBar.setTitle("");
                    bossBar.setProgress(0);
                    bossBar.setVisible(true);
                    attributes.attrSet("setup", true);
                }
                double progressPerc = progress.counter().progress();
                Key[] mergedPlaceholders = merge(placeholders, Key.of("progress", ProgressTracker.asPercentage(progressPerc)),
                    Key.of("elapsed", DataHelper.formTimeString(actor, Duration.ofMillis(elapsed))),
                    Key.of("current", progress.counter().current()), Key.of("amount", progress.counter().max()));
                if (detailed && detailedProgressMessage != null) {
                    actor.componentBuilder(detailedProgressMessage, mergedPlaceholders).send(actor, messageType);
                }
                if (ticks != 0) {
                    int delay = attributes.attrOrDefault("delay", Number.class, 0).intValue() + 1;
                    attributes.attrSet("delay", delay);
                    if (delay > ticks) {
                        return;
                    }
                    attributes.attrUnset("delay");
                }
                ComponentBuilder<?, ?> builder = ComponentBuilder
                    .parse(actor.getMessageManager().format(progressMessage, mergedPlaceholders));
                BossBar bossBar = actor.bossBar();
                bossBar.setProgress(progressPerc);
                bossBar.setTitle(builder.asLegacyText());

                int consoleDelay = attributes.attrOrDefault("consoleDelay", Number.class, 0).intValue() + 1;
                attributes.attrSet("consoleDelay", consoleDelay);
                if (consoleDelay > 4) {
                    return;
                }
                attributes.attrUnset("consoleDelay");
                if (!actor.isConsole()) {
                    builder.send(console);
                }
            });
        } else {
            progressNotifier = (progress, elapsed, detailed) -> {
                if (detailed && detailedProgressMessage != null) {
                    SubComponentBuilder<?, ?> builder = actor.componentBuilder(detailedProgressMessage,
                        merge(placeholders, Key.of("progress", ProgressTracker.asPercentage(progress)),
                            Key.of("elapsed", DataHelper.formTimeString(actor, Duration.ofMillis(elapsed))),
                            Key.of("current", progress.counter().current()), Key.of("amount", progress.counter().max())));
                    builder.send(actor, messageType);
                    if (!actor.isConsole()) {
                        builder.sendConsole();
                    }
                    return;
                }
                SubComponentBuilder<?, ?> builder = actor.componentBuilder(progressMessage,
                    merge(placeholders, Key.of("progress", ProgressTracker.asPercentage(progress)),
                        Key.of("elapsed", DataHelper.formTimeString(actor, Duration.ofMillis(elapsed))),
                        Key.of("current", progress.counter().current()), Key.of("amount", progress.counter().max())));
                builder.send(actor, messageType);
                if (!actor.isConsole()) {
                    builder.sendConsole();
                }
            };
        }
        if (doneMessage != null) {
            doneNotifier = (progress, elapsed) -> {
                SubComponentBuilder<?, ?> builder = actor.componentBuilder(doneMessage,
                    merge(placeholders, Key.of("elapsed", DataHelper.formTimeString(actor, Duration.ofMillis(elapsed))),
                        Key.of("current", progress.counter().current()), Key.of("amount", progress.counter().max())));
                builder.send(actor);
                if (!actor.isConsole()) {
                    builder.sendConsole();
                }
                if (actuallyUseBossBar && actor.hasBossBar()) {
                    actor.bossBar().setVisible(false);
                }
            };
        } else if (actuallyUseBossBar) {
            doneNotifier = (progress, elapsed) -> {
                if (actor.hasBossBar()) {
                    actor.bossBar().setVisible(false);
                }
            };
        }
        return new Tuple<>(progressNotifier, doneNotifier);
    }

    private Key[] merge(Key[] base, Key... add) {
        Key[] output = new Key[base.length + add.length];
        System.arraycopy(base, 0, output, 0, base.length);
        System.arraycopy(add, 0, output, base.length, add.length);
        return output;
    }

    private IMessage translate(MessageProvider provider) {
        if (provider == null) {
            return null;
        }
        return provider.getMessage(actor.getLanguage());
    }

}
