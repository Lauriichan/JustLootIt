package me.lauriichan.spigot.justlootit.convert;

import java.util.Random;

import org.bukkit.block.data.type.Chest;
import org.bukkit.persistence.PersistentDataContainer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.spigot.justlootit.JustLootItAccess;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoBlockEntity;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoChunk;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoWorld;
import me.lauriichan.spigot.justlootit.nms.util.Vec3i;
import me.lauriichan.spigot.justlootit.util.BlockUtil;
import me.lauriichan.spigot.justlootit.util.ConverterDataHelper;

public class MigrationConverter extends ChunkConverter {

    public MigrationConverter(VersionHandler versionHandler, ConversionProperties properties) {
        super(versionHandler, properties);
    }

    @Override
    public void convert(ProtoChunk chunk, Random random) {
        if (!chunk.getBlockEntities().isEmpty()) {
            ObjectArrayList<ProtoBlockEntity> allEntities = new ObjectArrayList<>(chunk.getBlockEntities());
            ObjectArrayList<ProtoBlockEntity> pendingBlockEntities = new ObjectArrayList<>(chunk.getBlockEntities());
            while (!pendingBlockEntities.isEmpty()) {
                ProtoBlockEntity state = pendingBlockEntities.remove(0);
                if (!(state.getData() instanceof Chest chest) || chest.getType() == Chest.Type.SINGLE) {
                    continue;
                }
                PersistentDataContainer dataContainer = state.getContainer();
                if (!JustLootItAccess.hasLegacyOffset(dataContainer)) {
                    continue;
                }
                JustLootItAccess.removeLegacyOffset(dataContainer);
                Vec3i location = state.getPos();
                Vec3i otherLocation = BlockUtil.findChestLocationAround(location.copy(), chest.getType(), chest.getFacing());
                ProtoBlockEntity otherState = allEntities.stream().filter(pending -> pending.getPos().equals(otherLocation))
                    .findFirst().orElse(null);
                if (otherState == null || !(otherState.getData() instanceof Chest otherChest)) {
                    // We convert double chests to single chests in this process
                    // The reason why is that we can't convert chests that are across borders
                    // Therefore we don't know how to convert this
                    Chest newChest = (Chest) chest.clone();
                    newChest.setType(Chest.Type.SINGLE);
                    state.setData(newChest);
                    chunk.updateBlock(state);
                    continue;
                }
                if (otherChest.getType() == Chest.Type.SINGLE) {
                    Chest newChest = (Chest) chest.clone();
                    newChest.setType(Chest.Type.SINGLE);
                    state.setData(newChest);
                    chunk.updateBlock(state);
                    continue;
                }
                pendingBlockEntities.remove(otherState);
                PersistentDataContainer otherDataContainer = otherState.getContainer();
                JustLootItAccess.removeLegacyOffset(otherDataContainer);
                ConverterDataHelper.setOffset(dataContainer, otherDataContainer, location, otherLocation);
                chunk.updateBlock(state);
                chunk.updateBlock(otherState);
            }
        }
    }

    @Override
    boolean isEnabled() {
        return properties.isProperty(ConvProp.DO_MIGRATION_CONVERSION);
    }

    @Override
    boolean isEnabledFor(ProtoWorld world) {
        return world.hasCapability(StorageCapability.class);
    }

}
