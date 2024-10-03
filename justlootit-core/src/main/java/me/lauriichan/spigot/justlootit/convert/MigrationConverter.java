package me.lauriichan.spigot.justlootit.convert;

import java.util.Comparator;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.convert.migration.provider.IBlockEntityMigration;
import me.lauriichan.spigot.justlootit.convert.migration.provider.IChunkMigration;
import me.lauriichan.spigot.justlootit.convert.migration.provider.IEntityMigration;
import me.lauriichan.spigot.justlootit.convert.migration.provider.IProtoMigration;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoBlockEntity;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoChunk;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoEntity;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoWorld;

public class MigrationConverter extends ChunkConverter {

    private final ObjectList<IChunkMigration> chunkMigrations;
    private final ObjectList<IBlockEntityMigration> blockMigrations;
    private final ObjectList<IEntityMigration> entityMigrations;

    public MigrationConverter(JustLootItPlugin plugin, VersionHandler versionHandler, ConversionProperties properties) {
        super(versionHandler, properties);
        ObjectList<IChunkMigration> chunkMigrations = new ObjectArrayList<>();
        ObjectList<IBlockEntityMigration> blockMigrations = new ObjectArrayList<>();
        ObjectList<IEntityMigration> entityMigrations = new ObjectArrayList<>();
        plugin.extension(IProtoMigration.class, true).callInstances(migration -> {
            if (migration instanceof IChunkMigration chunk) {
                chunkMigrations.add(chunk);
            }
            if (migration instanceof IBlockEntityMigration block) {
                blockMigrations.add(block);
            }
            if (migration instanceof IEntityMigration entity) {
                entityMigrations.add(entity);
            }
        });
        Comparator<IProtoMigration> comparator = (a, b) -> Integer.compare(a.priority(), b.priority());
        chunkMigrations.sort(comparator);
        blockMigrations.sort(comparator);
        entityMigrations.sort(comparator);
        this.chunkMigrations = ObjectLists.unmodifiable(chunkMigrations);
        this.blockMigrations = ObjectLists.unmodifiable(blockMigrations);
        this.entityMigrations = ObjectLists.unmodifiable(entityMigrations);
    }

    @Override
    public void convert(ProtoChunk chunk, Random random) {
        if (!blockMigrations.isEmpty() && !chunk.getBlockEntities().isEmpty()) {
            ObjectArrayList<ProtoBlockEntity> allEntities = new ObjectArrayList<>();
            ObjectArrayList<ProtoBlockEntity> pendingBlockEntities = new ObjectArrayList<>();
            Consumer<ProtoBlockEntity> queueRemover = pendingBlockEntities::remove;
            Function<Predicate<ProtoBlockEntity>, ProtoBlockEntity> findFilter = (predicate) -> allEntities.stream().filter(predicate)
                .findFirst().orElse(null);
            for (IBlockEntityMigration migration : blockMigrations) {
                allEntities.clear();
                allEntities.addAll(chunk.getBlockEntities());
                pendingBlockEntities.addAll(allEntities);
                while (!pendingBlockEntities.isEmpty()) {
                    migration.migrate(chunk, pendingBlockEntities.remove(0), random, queueRemover, findFilter);
                }
            }
        }
        if (!entityMigrations.isEmpty() && !chunk.getEntities().isEmpty()) {
            ObjectArrayList<ProtoEntity> pendingEntities = new ObjectArrayList<>();
            for (IEntityMigration migration : entityMigrations) {
                pendingEntities.addAll(chunk.getEntities());
                while (!pendingEntities.isEmpty()) {
                    migration.migrate(chunk, pendingEntities.remove(0), random);
                }
            }
        }
        if (!chunkMigrations.isEmpty()) {
            for (IChunkMigration migration : chunkMigrations) {
                migration.migrate(chunk, random);
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
