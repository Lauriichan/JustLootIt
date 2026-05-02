package me.lauriichan.spigot.justlootit.compatibility.provider.iris;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.loot.LootTable;

import com.volmit.iris.core.tools.IrisToolbelt;
import com.volmit.iris.engine.framework.Engine;
import com.volmit.iris.engine.object.IrisLootTable;
import com.volmit.iris.engine.object.IrisVanillaLootTable;
import com.volmit.iris.engine.platform.PlatformChunkGenerator;
import com.volmit.iris.util.collection.KList;

import me.lauriichan.spigot.justlootit.compatibility.data.iris.IIrisLootCache;
import me.lauriichan.spigot.justlootit.compatibility.data.iris.IIrisTableKey;
import me.lauriichan.spigot.justlootit.compatibility.data.iris.IrisLootTables;
import me.lauriichan.spigot.justlootit.util.CategorizedKeyMap;

public class IrisAccess implements IIrisAccess {

    private final String pluginId;

    public IrisAccess(String pluginId) {
        this.pluginId = pluginId;
    }

    @Override
    public boolean isIrisWorld(World world) {
        PlatformChunkGenerator generator = IrisToolbelt.access(world);
        return generator != null && !generator.isStudio();
    }

    @Override
    public IIrisLootCache loadLootTables(World world, IIrisTableKey[] keys) {
        Engine engine = IrisToolbelt.access(world).getEngine();
        KList<IrisLootTable> lootTables = new KList<>(keys.length);
        for (IIrisTableKey key : keys) {
            if (key instanceof IIrisTableKey.VanillaTableKey vanilla) {
                LootTable table = Bukkit.getLootTable(vanilla.key());
                if (table == null) {
                    continue;
                }
                lootTables.add(new IrisVanillaLootTable(table));
                continue;
            }
            IrisLootTable lootTable = engine.getData().getLootLoader().load(key.identifier());
            if (lootTable == null) {
                continue;
            }
            lootTables.add(lootTable);
        }
        return new IrisLootTables(lootTables);
    }

    @Override
    public boolean provideLootTableKeys(World world, CategorizedKeyMap keyMap) {
        PlatformChunkGenerator generator = IrisToolbelt.access(world);
        if (generator == null || generator.isStudio()) {
            return false;
        }
        String[] keys = generator.getData().getLootLoader().getPossibleKeys();
        for (String key : keys) {
            keyMap.add(pluginId, key);
        }
        return true;
    }

}
