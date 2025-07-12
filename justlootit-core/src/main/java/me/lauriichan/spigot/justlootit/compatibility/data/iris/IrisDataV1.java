package me.lauriichan.spigot.justlootit.compatibility.data.iris;

import com.volmit.iris.engine.framework.Engine;
import com.volmit.iris.engine.object.IrisLootTable;
import com.volmit.iris.util.collection.KList;

import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.compatibility.data.CompatibilityDataExtension;

public final class IrisDataV1 implements IIrisData {

    private final CompatibilityDataExtension<?> extension;
    private final IIrisTableKey[] keys;
    private final long seed;

    private volatile KList<IrisLootTable> lootTables;

    public IrisDataV1(final CompatibilityDataExtension<?> extension, final IIrisTableKey[] keys, final long seed) {
        this.extension = extension;
        this.keys = keys;
        this.seed = seed;
    }

    @Override
    public CompatibilityDataExtension<?> extension() {
        return extension;
    }

    @Override
    public int version() {
        return 0;
    }

    @Override
    public IIrisTableKey[] keys() {
        return keys;
    }
    
    @Override
    public long seed() {
        return seed;
    }

    @Override
    public KList<IrisLootTable> tables(Engine engine) {
        if (lootTables != null) {
            return lootTables;
        }
        KList<IrisLootTable> tables = new KList<>(keys.length);
        for (IIrisTableKey key : keys) {
            IrisLootTable table = key.lootTable(engine);
            if (table == null) {
                JustLootItPlugin.get().logger().warning("Failed to retrieve iris loot table '{0}'", key.identifier());
                continue;
            }
            tables.add(table);
        }
        return lootTables = tables;
    }

}
