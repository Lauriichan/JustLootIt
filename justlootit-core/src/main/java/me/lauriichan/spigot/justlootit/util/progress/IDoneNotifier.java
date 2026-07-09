package me.lauriichan.spigot.justlootit.util.progress;

import me.lauriichan.spigot.justlootit.storage.util.counter.CounterProgress;

public interface IDoneNotifier {

    void notify(CounterProgress progress, long elapsed);

}
