package me.lauriichan.spigot.justlootit.convert;

import java.util.Random;

import org.bukkit.block.data.type.Chest;
import org.bukkit.persistence.PersistentDataContainer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.spigot.justlootit.JustLootItAccess;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.data.FrameContainer;
import me.lauriichan.spigot.justlootit.data.IInventoryContainer;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoBlockEntity;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoChunk;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoEntity;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoWorld;
import me.lauriichan.spigot.justlootit.nms.util.Vec3i;
import me.lauriichan.spigot.justlootit.storage.IStorage;
import me.lauriichan.spigot.justlootit.storage.Stored;
import me.lauriichan.spigot.justlootit.util.EntityUtil;

public class ContainerRestorer extends ChunkConverter {

    public ContainerRestorer(VersionHandler versionHandler, ConversionProperties properties) {
        super(versionHandler, properties);
    }

    @Override
    public void convert(ProtoChunk chunk, Random random) {
        IStorage storage = chunk.getWorld().getCapability(StorageCapability.class).map(StorageCapability::storage).get();
        ObjectArrayList<ProtoBlockEntity> processed = new ObjectArrayList<>();
        for (ProtoBlockEntity blockEntity : chunk.getBlockEntities()) {
            if (processed.contains(blockEntity) || !blockEntity.hasInventory()) {
                continue;
            }
            processed.add(blockEntity);
            PersistentDataContainer dataContainer = blockEntity.getContainer();
            ProtoBlockEntity otherEntity = null;
            if (!JustLootItAccess.hasIdentity(dataContainer)) {
                if (!(blockEntity.getData() instanceof Chest chest) || chest.getType() == Chest.Type.SINGLE
                    || !JustLootItAccess.hasAnyOffset(dataContainer)) {
                    continue;
                }
                Vec3i offset = JustLootItAccess.getAnyOffset(dataContainer);
                otherEntity = chunk.getBlockEntity(blockEntity.getPos().add(offset));
                if (!JustLootItAccess.hasIdentity(otherEntity.getContainer())) {
                    continue;
                }
            }
            PersistentDataContainer identityDataContainer = otherEntity != null ? otherEntity.getContainer() : dataContainer;
            long containerId = JustLootItAccess.getIdentity(identityDataContainer);
            Stored<IInventoryContainer> stored = storage.read(containerId);
            if (stored == null || stored.isEmpty()) {
                continue;
            }
            IInventoryContainer container = stored.value();
            if (!container.canBeRestored()) {
                logger.warning("Type '{0}' doesn't support restoration yet", container.getClass().getSimpleName());
                continue;
            }
            container.restore(logger, versionHandler, otherEntity != null ? blockEntity : otherEntity);
            if (storage.delete(containerId)) {
                if (otherEntity != null) {
                    JustLootItAccess.removeOffset(dataContainer);
                    chunk.updateBlock(otherEntity);
                }
                JustLootItAccess.removeIdentity(identityDataContainer);
                chunk.updateBlock(blockEntity);
            }
        }
        processed.clear();
        for (ProtoEntity entity : chunk.getEntities()) {
            PersistentDataContainer dataContainer = entity.getContainer();
            if (!JustLootItAccess.hasIdentity(dataContainer)) {
                continue;
            }
            if (EntityUtil.isItemFrame(entity.getType())) {
                long containerId = JustLootItAccess.getIdentity(dataContainer);
                Stored<FrameContainer> stored = storage.read(containerId);
                if (stored == null || stored.isEmpty()) {
                    continue;
                }
                entity.getNbt().set("Item", nbtHelper.asTag(stored.value().getItem()));
                if (storage.delete(containerId)) {
                    JustLootItAccess.removeIdentity(dataContainer);
                    chunk.updateEntity(entity);
                }
            } else if (EntityUtil.isSupportedEntity(entity.getType())) {
                long containerId = JustLootItAccess.getIdentity(dataContainer);
                Stored<IInventoryContainer> stored = storage.read(containerId);
                if (stored == null || stored.isEmpty()) {
                    continue;
                }
                IInventoryContainer container = stored.value();
                if (!container.canBeRestored()) {
                    logger.warning("Type '{0}' doesn't support restoration yet", container.getClass().getSimpleName());
                    continue;
                }
                container.restore(logger, versionHandler, entity);
                if (storage.delete(containerId)) {
                    JustLootItAccess.removeIdentity(dataContainer);
                    chunk.updateEntity(entity);
                }
            }
        }
    }

    @Override
    boolean isEnabled() {
        return true;
    }

    @Override
    boolean isEnabledFor(ProtoWorld world) {
        return world.hasCapability(StorageCapability.class);
    }

}
