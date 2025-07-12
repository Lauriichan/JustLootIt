package me.lauriichan.spigot.justlootit.compatibility.data.iris;

import org.bukkit.World;
import org.bukkit.inventory.Inventory;

import com.volmit.iris.core.tools.IrisToolbelt;
import com.volmit.iris.engine.object.InventorySlotType;
import com.volmit.iris.engine.object.IrisLootTable;
import com.volmit.iris.util.collection.KList;
import com.volmit.iris.util.math.RNG;

public record IrisLootTables(KList<IrisLootTable> lootTables) implements IIrisLootCache {

    @Override
    public boolean isNotEmpty() {
        return lootTables.isNotEmpty();
    }

    @Override
    public void fill(Inventory inventory, long seed, World world, int x, int y, int z) {
        IrisToolbelt.access(world).getEngine().addItems(false, inventory, new RNG(seed), lootTables, InventorySlotType.STORAGE, world, x, y,
            z, 0);
    }

}
