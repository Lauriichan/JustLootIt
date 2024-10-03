package me.lauriichan.spigot.justlootit.convert.migration.provider;

import me.lauriichan.minecraft.pluginbase.extension.ExtensionPoint;
import me.lauriichan.minecraft.pluginbase.extension.IExtension;

@ExtensionPoint
public sealed interface IProtoMigration extends IExtension permits IBlockEntityMigration, IChunkMigration, IEntityMigration {
    
    default int priority() {
        return 0;
    }

}
