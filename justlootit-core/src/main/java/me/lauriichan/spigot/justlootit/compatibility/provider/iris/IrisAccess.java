package me.lauriichan.spigot.justlootit.compatibility.provider.iris;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.loot.LootTable;

import com.volmit.iris.core.tools.IrisToolbelt;
import com.volmit.iris.engine.framework.Engine;
import com.volmit.iris.engine.object.IrisLootTable;
import com.volmit.iris.engine.object.IrisVanillaLootTable;
import com.volmit.iris.util.collection.KList;

import me.lauriichan.spigot.justlootit.compatibility.data.iris.IIrisLootCache;
import me.lauriichan.spigot.justlootit.compatibility.data.iris.IIrisTableKey;
import me.lauriichan.spigot.justlootit.compatibility.data.iris.IrisLootTables;

public class IrisAccess implements IIrisAccess {

    @Override
    public boolean isIrisWorld(World world) {
        return IrisToolbelt.isIrisWorld(world);
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

}
