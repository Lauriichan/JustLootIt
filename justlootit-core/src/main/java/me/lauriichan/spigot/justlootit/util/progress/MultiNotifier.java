package me.lauriichan.spigot.justlootit.util.progress;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.spigot.justlootit.storage.util.Tuple;
import me.lauriichan.spigot.justlootit.storage.util.counter.CounterProgress;

public final class MultiNotifier implements IDoneNotifier, IProgressNotifier {

    private final ObjectList<IDoneNotifier> doneNotifier = ObjectLists.synchronize(new ObjectArrayList<>());
    private final ObjectList<IProgressNotifier> progressNotifier = ObjectLists.synchronize(new ObjectArrayList<>());

    @Override
    public void notify(CounterProgress progress, long elapsed) {
        for (IDoneNotifier notifier : doneNotifier) {
            notifier.notify(progress, elapsed);
        }
        doneNotifier.clear();
        progressNotifier.clear();
    }

    @Override
    public void notify(CounterProgress progress, long elapsed, boolean detailed) {
        for (IProgressNotifier notifier : progressNotifier) {
            notifier.notify(progress, elapsed, detailed);
        }
    }

    public void register(Tuple<IProgressNotifier, IDoneNotifier> notifiers) {
        if (notifiers == null) {
            return;
        }
        register(notifiers.first());
        register(notifiers.second());
    }

    public void register(IDoneNotifier doneNotifier) {
        if (doneNotifier == null) {
            return;
        }
        this.doneNotifier.add(doneNotifier);
    }

    public void register(IProgressNotifier progressNotifier) {
        if (progressNotifier == null) {
            return;
        }
        this.progressNotifier.add(progressNotifier);
    }

    public void unregister(Tuple<IProgressNotifier, IDoneNotifier> notifiers) {
        if (notifiers == null) {
            return;
        }
        unregister(notifiers.first());
        unregister(notifiers.second());
    }

    public void unregister(IDoneNotifier doneNotifier) {
        if (doneNotifier == null) {
            return;
        }
        this.doneNotifier.remove(doneNotifier);
    }

    public void unregister(IProgressNotifier progressNotifier) {
        if (progressNotifier == null) {
            return;
        }
        this.progressNotifier.remove(progressNotifier);
    }

}
