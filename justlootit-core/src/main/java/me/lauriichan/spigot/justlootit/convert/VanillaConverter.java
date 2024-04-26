package me.lauriichan.spigot.justlootit.convert;

import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoChunk;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoEntity;
import me.lauriichan.spigot.justlootit.util.EntityUtil;

public class VanillaConverter extends ChunkConverter {

    public VanillaConverter(ConversionProperties properties) {
        super(properties);
    }

    @Override
    public void convert(ProtoChunk chunk) {
        if (!chunk.getBlockEntities().isEmpty()) {
            ObjectArrayList<BlockState> pendingBlockEntities = new ObjectArrayList<>(chunk.getBlockEntities());
        }
        for (ProtoEntity entity : chunk.getEntities()) {
            EntityType type = entity.getType();
            if (EntityUtil.isItemFrame(type)) {
                if (!properties.isProperty(ConvProp.VANILLA_ALLOW_STATIC_CONTAINER)) {
                    continue;
                }
                // TODO: Implement item frame conversion
                continue;
            }
            if (EntityUtil.isSuppportedEntity(type)) {
                
                
            }
        }
    }

    @Override
    boolean isEnabled() {
        return properties.isProperty(ConvProp.DO_VANILLA_CONVERSION);
    }

}
