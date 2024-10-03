package me.lauriichan.spigot.justlootit.convert.migration;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.bukkit.block.data.type.Chest;
import org.bukkit.persistence.PersistentDataContainer;

import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.JustLootItAccess;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.convert.migration.provider.IBlockEntityMigration;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoBlockEntity;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoChunk;
import me.lauriichan.spigot.justlootit.nms.util.Vec3i;
import me.lauriichan.spigot.justlootit.storage.IStorage;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.util.BlockUtil;
import me.lauriichan.spigot.justlootit.util.ConverterDataHelper;

@Extension
public final class M1_OffsetV1 implements IBlockEntityMigration {
    
    @Override
    public int priority() {
        return 1;
    }

    @Override
    public void migrate(ProtoChunk chunk, ProtoBlockEntity state, Random random, Consumer<ProtoBlockEntity> queueRemover,
        Function<Predicate<ProtoBlockEntity>, ProtoBlockEntity> findFilter) {
        if (!(state.getData() instanceof Chest chest) || chest.getType() == Chest.Type.SINGLE) {
            return;
        }
        PersistentDataContainer dataContainer = state.getContainer();
        if (!JustLootItAccess.hasOffsetV1(dataContainer)) {
            return;
        }
        JustLootItAccess.removeOffsetV1(dataContainer);
        Vec3i location = state.getPos();
        Vec3i otherLocation = BlockUtil.findChestLocationAround(location.copy(), chest.getType(), chest.getFacing());
        ProtoBlockEntity otherState = findFilter.apply(pending -> pending.getPos().equals(otherLocation));
        if (otherState == null || !(otherState.getData() instanceof Chest otherChest)) {
            // We convert double chests to single chests in this process
            // The reason why is that we can't convert chests that are across borders
            // Therefore we don't know how to convert this
            Chest newChest = (Chest) chest.clone();
            newChest.setType(Chest.Type.SINGLE);
            state.setData(newChest);
            chunk.updateBlock(state);
            return;
        }
        if (otherChest.getType() == Chest.Type.SINGLE) {
            Chest newChest = (Chest) chest.clone();
            newChest.setType(Chest.Type.SINGLE);
            state.setData(newChest);
            chunk.updateBlock(state);
            return;
        }
        queueRemover.accept(otherState);
        PersistentDataContainer otherDataContainer = otherState.getContainer();
        JustLootItAccess.removeOffsetV1(otherDataContainer);
        ConverterDataHelper.setOffset(dataContainer, otherDataContainer, location, otherLocation);
        
        if (JustLootItAccess.hasIdentity(dataContainer) && JustLootItAccess.hasIdentity(otherDataContainer)) {
            IStorage<Storable> storage = chunk.getWorld().getCapability(StorageCapability.class).map(StorageCapability::storage).get();
            long id = JustLootItAccess.getIdentity(dataContainer);
            long otherId = JustLootItAccess.getIdentity(otherDataContainer);
            if (id > otherId) {
                JustLootItAccess.removeIdentity(dataContainer);
                storage.delete(id);
            } else if (id != otherId) {
                JustLootItAccess.removeIdentity(otherDataContainer);
                storage.delete(otherId);
            }
        }
        
        chunk.updateBlock(state);
        chunk.updateBlock(otherState);
    }

}
