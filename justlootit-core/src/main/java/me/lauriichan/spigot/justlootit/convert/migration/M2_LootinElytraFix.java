package me.lauriichan.spigot.justlootit.convert.migration;

import java.util.Random;

import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.JustLootItAccess;
import me.lauriichan.spigot.justlootit.convert.migration.provider.IEntityMigration;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoChunk;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoEntity;
import me.lauriichan.spigot.justlootit.nms.nbt.ICompoundTag;
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;
import me.lauriichan.spigot.justlootit.util.EntityUtil;

@Extension
public class M2_LootinElytraFix implements IEntityMigration {
    
    @Override
    public int priority() {
        return 2;
    }

    @Override
    public void migrate(ProtoChunk chunk, ProtoEntity entity, Random random) {
        if (!EntityUtil.isItemFrame(entity.getType()) || !JustLootItAccess.hasIdentity(entity.getContainer())) {
            return;
        }
        ICompoundTag tag = entity.getNbt();
        if (tag.has("Item", TagType.COMPOUND)) {
            tag.remove("Item");
            chunk.updateEntity(entity);
        }
    }

}
