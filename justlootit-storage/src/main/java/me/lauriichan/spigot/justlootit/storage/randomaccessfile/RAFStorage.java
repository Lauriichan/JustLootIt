package me.lauriichan.spigot.justlootit.storage.randomaccessfile;

import java.io.File;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.longs.Long2LongArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.storage.Storage;

public class RAFStorage<S extends Storable> extends Storage<S> {

    private final File directory;
    private final Long2ObjectArrayMap<RAFAccess<?>> accesses = new Long2ObjectArrayMap<>();

    public RAFStorage(Class<S> baseType, File directory) {
        super(baseType);
        this.directory = directory;
    }

    @Override
    public void updateEach(Consumer<S> updater) {
        // TODO Auto-generated method stub

    }

    @Override
    public void write(S storable) {
        long fileId = storable.id() >> 10;

    }

    @Override
    public S read(long id) {
        return null;
    }

}
