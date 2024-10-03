package me.lauriichan.spigot.justlootit.convert.migration.provider;

import java.util.Random;

import me.lauriichan.spigot.justlootit.nms.convert.ProtoChunk;

public non-sealed interface IChunkMigration extends IProtoMigration {

    void migrate(ProtoChunk chunk, Random random);

}
