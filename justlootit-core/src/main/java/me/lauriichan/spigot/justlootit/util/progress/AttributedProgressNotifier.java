package me.lauriichan.spigot.justlootit.util.progress;

import me.lauriichan.minecraft.pluginbase.util.attribute.Attributes;
import me.lauriichan.spigot.justlootit.storage.util.counter.CounterProgress;

record AttributedProgressNotifier(Attributes attributes, IAttributedProgressNotifier notifier) implements IProgressNotifier {

    AttributedProgressNotifier(IAttributedProgressNotifier notifier) {
        this(new Attributes(), notifier);
    }

    @Override
    public void notify(CounterProgress progress, long elapsed, boolean detailed) {
        notifier.notify(attributes, progress, elapsed, detailed);
    }

}