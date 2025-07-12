package me.lauriichan.spigot.justlootit.compatibility.data.iris;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;

import com.volmit.iris.core.tools.IrisToolbelt;
import com.volmit.iris.engine.framework.Engine;
import com.volmit.iris.engine.object.InventorySlotType;
import com.volmit.iris.engine.object.IrisLootTable;
import com.volmit.iris.engine.platform.PlatformChunkGenerator;
import com.volmit.iris.util.collection.KList;
import com.volmit.iris.util.math.RNG;

import me.lauriichan.laylib.localization.Key;
import me.lauriichan.spigot.justlootit.compatibility.data.ICompatibilityData;
import me.lauriichan.spigot.justlootit.data.CompatibilityContainer;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;

public interface IIrisData extends ICompatibilityData {

    IIrisTableKey[] keys();

    KList<IrisLootTable> tables(Engine engine);

    long seed();

    @Override
    default boolean canFill(BlockState state, Location location) {
        PlatformChunkGenerator generator = IrisToolbelt.access(state.getWorld());
        if (generator == null) {
            return false;
        }
        return tables(generator.getEngine()).isNotEmpty();
    }

    @Override
    default boolean fill(CompatibilityContainer container, PlayerAdapter player, BlockState state, Location location, Inventory inventory) {
        Engine engine = IrisToolbelt.access(state.getWorld()).getEngine();
        engine.addItems(false, inventory, new RNG(seed()), tables(engine), InventorySlotType.STORAGE, state.getWorld(),
            location.getBlockX(), location.getBlockY(), location.getBlockZ(), 0);
        return true;
    }
    
    @Override
    default void addInfoData(Consumer<Key> add) {
        add.accept(Key.of("Seed", seed()));
        for (IIrisTableKey key : keys()) {
            add.accept(Key.of("Loot Tables", key.identifier()));
        }
    }

}
