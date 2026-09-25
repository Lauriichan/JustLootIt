package me.lauriichan.spigot.justlootit.compatibility.support;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

public abstract class AbstractSupport<T> {

    protected final ObjectList<T> implementations = ObjectLists.synchronize(new ObjectArrayList<>());

    public final void register(T implementation) {
        if (implementations.contains(implementation)) {
            throw new IllegalStateException("Already registered implementation");
        }
        implementations.add(implementation);
    }

    public final void unregister(T implementation) {
        this.implementations.remove(implementation);
    }

}
