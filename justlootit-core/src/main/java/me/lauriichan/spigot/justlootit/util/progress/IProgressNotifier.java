package me.lauriichan.spigot.justlootit.util.progress;

import me.lauriichan.minecraft.pluginbase.util.attribute.Attributes;
import me.lauriichan.spigot.justlootit.storage.util.counter.CounterProgress;

public interface IProgressNotifier {

    public static interface IAttributedProgressNotifier {

        void notify(Attributes attributes, CounterProgress progress, long elapsed, boolean detailed);

    }

    void notify(CounterProgress progress, long elapsed, boolean detailed);
    
    public static IProgressNotifier attributed(IAttributedProgressNotifier notifier) {
        return new AttributedProgressNotifier(notifier);
    }

}
