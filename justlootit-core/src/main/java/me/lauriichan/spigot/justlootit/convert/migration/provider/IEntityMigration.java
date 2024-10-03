package me.lauriichan.spigot.justlootit.convert.migration.provider;

import java.util.Random;

import me.lauriichan.spigot.justlootit.nms.convert.ProtoChunk;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoEntity;

public non-sealed interface IEntityMigration extends IProtoMigration {
    
    void migrate(ProtoChunk chunk, ProtoEntity entity, Random random);

}
