package me.lauriichan.spigot.justlootit.compatibility.data.iris;

import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.loot.LootTable;

import com.volmit.iris.engine.framework.Engine;
import com.volmit.iris.engine.object.IrisLootTable;
import com.volmit.iris.engine.object.IrisVanillaLootTable;
import com.volmit.iris.util.collection.KList;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.spigot.justlootit.config.world.WorldConfig;

public interface IIrisTableKey extends Comparable<IIrisTableKey> {

    static IIrisTableKey[] create(String pluginId, WorldConfig config, KList<IrisLootTable> lootTables) {
        ObjectArrayList<IIrisTableKey> keys = new ObjectArrayList<>(lootTables.size());
        for (IrisLootTable table : lootTables) {
            if (table instanceof IrisVanillaLootTable vanilla) {
                if (config.isLootTableBlacklisted(vanilla.getLootTable().getKey())) {
                    continue;
                }
                keys.add(new VanillaTableKey(vanilla.getLootTable().getKey()));
                continue;
            }
            if (config.isLootTableBlacklisted(pluginId, table.getLoadKey())) {
                continue;
            }
            keys.add(new IrisTableKey(table.getLoadKey()));

        }
        Collections.sort(keys);
        return keys.toArray(IIrisTableKey[]::new);
    }

    IrisLootTable lootTable(Engine engine);

    String identifier();

    record VanillaTableKey(NamespacedKey key) implements IIrisTableKey {

        @Override
        public IrisLootTable lootTable(Engine engine) {
            LootTable table = Bukkit.getLootTable(key);
            if (table == null) {
                return null;
            }
            return new IrisVanillaLootTable(table);
        }

        @Override
        public String identifier() {
            return key.toString();
        }

        @Override
        public int compareTo(IIrisTableKey o) {
            if (o instanceof VanillaTableKey) {
                return 0;
            }
            return -1;
        }

    }

    record IrisTableKey(String key) implements IIrisTableKey {

        @Override
        public IrisLootTable lootTable(Engine engine) {
            return engine.getData().getLootLoader().load(key);
        }

        @Override
        public String identifier() {
            return key;
        }

        @Override
        public int compareTo(IIrisTableKey o) {
            if (o instanceof IrisTableKey) {
                return 0;
            }
            return 1;
        }

    }

}
