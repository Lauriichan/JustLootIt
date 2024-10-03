package me.lauriichan.spigot.justlootit.convert.migration.provider;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import me.lauriichan.spigot.justlootit.nms.convert.ProtoBlockEntity;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoChunk;

public non-sealed interface IBlockEntityMigration extends IProtoMigration {

    void migrate(ProtoChunk chunk, ProtoBlockEntity entity, Random random, Consumer<ProtoBlockEntity> queueRemover,
        Function<Predicate<ProtoBlockEntity>, ProtoBlockEntity> findFilter);

}
